package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import kotlinx.coroutines.flow.StateFlow


interface PrintTicketUseCase {
    val printState: StateFlow<PrintResult?>

    operator fun invoke(ticket: Ticket)

    fun dismissResult()
}