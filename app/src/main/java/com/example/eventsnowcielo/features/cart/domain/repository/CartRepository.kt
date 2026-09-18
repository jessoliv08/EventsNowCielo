package com.example.eventsnowcielo.features.cart.domain.repository

import com.example.eventsnowcielo.features.cart.domain.model.CartItem

interface CartRepository {
    suspend fun addItem(item: CartItem)
}
