package com.example.eventsnowcielo.core.database.orders.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cieloOrderId: String,
    val transactionId: String? = null,
    val paymentCode: String? = null,
    val email: String? = null,
    val ec: String? = null,
    val installments: Int = 1,
    val purchasedAt: Long = System.currentTimeMillis()
)