package com.example.eventsnowcielo.features.cart.data

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import org.koin.core.annotation.Single

@Single(binds = [CartRepository::class])
class CartRepositoryImpl : CartRepository {

    private val items = mutableListOf<CartItem>()

    override suspend fun addItem(item: CartItem) {
        val existingIndex = items.indexOfFirst { it.eventId == item.eventId }
        if (existingIndex >= 0) {
            val existing = items[existingIndex]
            items[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            items.add(item)
        }
    }
}
