package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.core.ui.util.formatPriceInCents
import com.example.eventsnowcielo.features.purchases.domain.model.PaymentWithTickets
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.domain.repository.PurchasesRepository
import com.example.eventsnowcielo.features.purchases.domain.repository.TicketPrinterRepository
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import java.text.NumberFormat
import java.util.Locale

@Single(binds = [PrintTicketUseCase::class])
class PrintTicketUseCaseImpl(
    private val ticketPrinterRepository: TicketPrinterRepository,
    private val purchasesRepository: PurchasesRepository
): PrintTicketUseCase {
    override val printState: StateFlow<PrintResult?> = ticketPrinterRepository.printState

    override fun printTickets(paymentWithTickets: PaymentWithTickets) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterRepository.printTicket(ticketsReceipt(paymentWithTickets), alignCenter)
    }

    override suspend fun printTicket(ticket: Ticket) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterRepository.printTicket(
            information = ticketReceipt(ticket),
            alignCenter
        )
    }

    override fun dismissResult() {
        ticketPrinterRepository.dismissResult()
    }

    override suspend fun ticketReceipt(ticket: Ticket): String {
        return purchasesRepository.getPaymentByTicket(ticket)?.let {
            ticketsReceipt(it)
        } ?: "Some error while processing Receipt"
    }

    override fun ticketsReceipt(paymentWithTickets: PaymentWithTickets): String {

        return buildString {
            appendLine("======================================")
            appendLine("         EVENTS NOW - RECEIPT    ")
            appendLine("======================================")
            appendLine()

            paymentWithTickets.tickets.forEachIndexed { index, ticket ->
                appendLine("TICKET #${index + 1}")
                appendLine(ticket.toHumanReadableString())
                if (index < paymentWithTickets.tickets.lastIndex) {
                    appendLine("======================================")
                    appendLine()
                }
            }
            appendLine()
            appendLine("======================================")
            appendLine()
            appendLine("Payment Information:")
            appendLine("Order ID: ${paymentWithTickets.payment.orderId}")
            appendLine("Transaction ID: ${paymentWithTickets.payment.transactionId}")
            appendLine("Payment chosen: ${paymentWithTickets.payment.paymentCode}")
            if (paymentWithTickets.payment.installments > 1) {
                appendLine("Payment installments: ${paymentWithTickets.payment.installments}")
            }
            appendLine("EC: ${paymentWithTickets.payment.ec}")
            appendLine("Email: ${paymentWithTickets.payment.email}")
            appendLine()
            appendLine("======================================")
            appendLine("TOTAL TICKETS: ${paymentWithTickets.tickets.size}")
            val grandTotalInCents = paymentWithTickets.tickets.sumOf { it.totalAmountInCents }
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
        ======================================
    """.trimIndent()
}