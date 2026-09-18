package com.example.eventsnowcielo.features.cart.domain.model

import com.example.eventsnowcielo.features.events.domain.model.Event

data class CartItem(
    val event: Event,
    val quantity: Int
) {
    val lineTotalInCents: Long
        get() = event.priceInCents * quantity
}
