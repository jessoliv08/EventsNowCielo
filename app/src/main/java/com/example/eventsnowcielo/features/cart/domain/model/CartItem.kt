package com.example.eventsnowcielo.features.cart.domain.model

data class CartItem(
    val eventId: String,
    val title: String,
    val imageUrl: String,
    val priceInCents: Long,
    val quantity: Int
)
