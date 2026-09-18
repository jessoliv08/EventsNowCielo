package com.example.eventsnowcielo.core.database.orders

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val eventId: String,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val quantity: Int,
    val unitPriceInCents: Long,
    val totalAmountInCents: Long,
    val status: String,
    val transactionId: String? = null,
    val purchasedAt: Long = System.currentTimeMillis()
)
