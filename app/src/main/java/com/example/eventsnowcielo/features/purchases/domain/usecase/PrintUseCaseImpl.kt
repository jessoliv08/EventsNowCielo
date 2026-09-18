package com.example.eventsnowcielo.features.purchases.domain.usecase

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

    override operator fun invoke(ticket: Ticket) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterRepository.printTicket(ticket.toHumanReadableString(), alignCenter)
    }

    override fun dismissResult() {
        ticketPrinterRepository.dismissResult()
    }
}

fun Ticket.toHumanReadableString(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val unitPriceFormatted = currencyFormat.format(unitPriceInCents / 100.0)
    val totalAmountFormatted = currencyFormat.format(totalAmountInCents / 100.0)

    val statusText = if (isPastEvent) "Finished / Past Event" else "Upcoming Event"

    return """
        ========================================
        🎟️ TICKET DETAILS - $title
        ========================================
        Date: $date
        Time: $time
        Status: $statusText
        
        Quantity: $quantity ticket(s)
        Unit Price: $unitPriceFormatted
        Total Paid: $totalAmountFormatted
        
        ----------------------------------------
        Order ID: $orderId
        ${transactionId?.let { "Transaction ID: $it" } ?: "Transaction ID: N/A"}
        ========================================
    """.trimIndent()
}