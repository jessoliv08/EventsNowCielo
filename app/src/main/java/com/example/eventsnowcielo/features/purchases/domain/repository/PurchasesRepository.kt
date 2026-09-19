package com.example.eventsnowcielo.features.purchases.domain.repository

import com.example.eventsnowcielo.features.purchases.domain.model.PaymentWithTickets
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface PurchasesRepository {
    fun getCompletedTickets(): Flow<List<Ticket>>
    fun getCompletedOrdersCount(): Flow<Int>
    suspend fun getPaymentByTicket(ticket: Ticket): PaymentWithTickets?
}