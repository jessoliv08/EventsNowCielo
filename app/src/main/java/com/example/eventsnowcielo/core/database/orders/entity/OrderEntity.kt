package com.example.eventsnowcielo.core.database.orders.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventsnowcielo.core.database.orders.entity.PaymentEntity

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("paymentId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val quantity: Int,
    val unitPriceInCents: Long,
    val totalAmountInCents: Long,
    val status: String,
    val purchasedAt: Long = System.currentTimeMillis(),
    val paymentId: Long,
)