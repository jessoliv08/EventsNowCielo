package com.example.eventsnowcielo.features.purchases.domain

import kotlinx.coroutines.flow.Flow

interface PurchasesRepository {
    fun getCompletedTickets(): Flow<List<Ticket>>
    fun getCompletedOrdersCount(): Flow<Int>
}
