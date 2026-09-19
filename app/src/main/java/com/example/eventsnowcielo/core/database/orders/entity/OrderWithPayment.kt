package com.example.eventsnowcielo.core.database.orders.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PaymentWithOrders(
    @Embedded val payment: PaymentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "paymentId"
    )
    val orders: List<OrderEntity>
)