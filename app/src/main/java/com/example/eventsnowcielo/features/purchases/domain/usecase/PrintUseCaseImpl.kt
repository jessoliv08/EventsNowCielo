package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.core.ui.util.formatPriceInCents
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.domain.repository.TicketPrinterRepository
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import java.text.NumberFormat
import java.util.Locale

@Single(binds = [PrintTicketUseCase::class])
class PrintTicketUseCaseImpl(
    private val ticketPrinterRepository: TicketPrinterRepository
): PrintTicketUseCase {
    override val printState: StateFlow<PrintResult?> = ticketPrinterRepository.printState

    override fun printTicket(ticket: Ticket) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterRepository.printTicket(ticket.toHumanReadableString(), alignCenter)
    }

    override fun printTickets(tickets: List<Ticket>) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterRepository.printTicket(ticketsReceipt(tickets), alignCenter)
    }

    override fun dismissResult() {
        ticketPrinterRepository.dismissResult()
    }

    override fun ticketReceipt(ticket: Ticket): String {
        return ticket.toHumanReadableString()
    }

    override fun ticketsReceipt(tickets: List<Ticket>): String {
        if (tickets.isEmpty()) return "NO TICKETS FOUND"

        return buildString {
            appendLine("======================================")
            appendLine("         EVENTS NOW - RECEIPT    ")
            appendLine("======================================")
            appendLine()

            tickets.forEachIndexed { index, ticket ->
                appendLine("TICKET #${index + 1}")
                appendLine(ticket.toHumanReadableString())
                if (index < tickets.lastIndex) {
                    appendLine("======================================")
                    appendLine()
                }
            }

            appendLine()
            appendLine("======================================")
            appendLine("TOTAL TICKETS: ${tickets.size}")
            val grandTotalInCents = tickets.sumOf { it.totalAmountInCents }
            appendLine("GRAND TOTAL: ${formatPriceInCents(grandTotalInCents)}")
            appendLine("======================================")
        }
    }
}

fun Ticket.toHumanReadableString(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val unitPriceFormatted = currencyFormat.format(unitPriceInCents / 100.0)
    val totalAmountFormatted = currencyFormat.format(totalAmountInCents / 100.0)

    val statusText = if (isPastEvent) "Finished / Past Event" else "Upcoming Event"

    return """
        ======================================
        🎟️ TICKET DETAILS - $title
        ======================================
        Date: $date
        Time: $time
        Status: $statusText
        
        Quantity: $quantity ticket(s)
        Unit Price: $unitPriceFormatted
        Total Paid: $totalAmountFormatted
        
        ----------------------------------------
        Order ID: $orderId
        ${transactionId?.let { "Transaction ID: $it" } ?: "Transaction ID: N/A"}
        ======================================
    """.trimIndent()
}