package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import kotlinx.coroutines.flow.StateFlow


interface PrintTicketUseCase {
    val printState: StateFlow<PrintResult?>

    fun printTicket(ticket: Ticket)
    fun printTickets(tickets: List<Ticket>)

    fun dismissResult()
    fun ticketReceipt(ticket: Ticket): String
    fun ticketsReceipt(tickets: List<Ticket>): String
}