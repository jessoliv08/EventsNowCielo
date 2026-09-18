package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.features.purchases.data.TicketPrinterManager
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import java.text.NumberFormat
import java.util.Locale

@Single
class PrintTicketUseCase(
    private val ticketPrinterManager: TicketPrinterManager
) {
    val printState: StateFlow<PrintResult?> = ticketPrinterManager.printState

    operator fun invoke(ticket: Ticket) {
        val alignCenter = HashMap<String, Int>().apply {
            put("align", 1) // 1 = Center alignment in Cielo SDK
        }
        ticketPrinterManager.printTicket(ticket.toHumanReadableString(), alignCenter)
    }

    fun dismissResult() {
        ticketPrinterManager.dismissResult()
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