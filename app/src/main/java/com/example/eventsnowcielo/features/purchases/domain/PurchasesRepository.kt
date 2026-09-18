package com.example.eventsnowcielo.features.purchases.domain

import com.example.eventsnowcielo.core.database.orders.OrderEntity
import kotlinx.coroutines.flow.Flow

interface PurchasesRepository {
    fun getUpcomingPurchases(): Flow<List<OrderEntity>>
    fun getPastPurchases(): Flow<List<OrderEntity>>
}
