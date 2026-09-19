package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.features.purchases.domain.model.PaymentWithTickets
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import kotlinx.coroutines.flow.StateFlow


interface PrintTicketUseCase {
    val printState: StateFlow<PrintResult?>

    fun printTickets(paymentWithTickets: PaymentWithTickets)
    suspend fun printTicket(ticket: Ticket)

    fun dismissResult()
    fun ticketsReceipt(paymentWithTickets: PaymentWithTickets): String
    suspend fun ticketReceipt(ticket: Ticket): String
}