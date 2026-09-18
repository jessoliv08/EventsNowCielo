package com.example.eventsnowcielo.core.database.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val eventId: String,
    val title: String,
    val description: String,
    val priceInCents: Long,
    val quantity: Int,
    val imageUrl: String,
    val date: String,
    val time: String,
    val location: String,
    val categoryId: String,
    val categoryName: String
)
