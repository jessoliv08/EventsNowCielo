package com.example.eventsnowcielo.features.cart.domain.repository

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.events.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<List<CartItem>>

    suspend fun addToCart(event: Event, quantity: Int)

    suspend fun removeFromCart(eventId: String)

    suspend fun updateQuantity(eventId: String, quantity: Int)

    suspend fun clearCart()

    suspend fun getCart(): List<CartItem>
}
