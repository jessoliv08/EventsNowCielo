package com.example.eventsnowcielo.features.cart.data

import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import com.example.eventsnowcielo.features.events.domain.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [CartRepository::class])
class CartRepositoryImpl(
    private val cartDao: CartDao
) : CartRepository {

    override val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addToCart(event: Event, quantity: Int) {
        if (quantity <= 0) return

        val existingItem = cartDao.getItemByEventId(event.id)
        if (existingItem != null) {
            cartDao.insertOrUpdateItem(
                existingItem.copy(quantity = existingItem.quantity + quantity)
            )
        } else {
            cartDao.insertOrUpdateItem(event.toCartItemEntity(quantity))
        }
    }

    override suspend fun removeFromCart(eventId: String) {
        cartDao.deleteItem(eventId)
    }

    override suspend fun updateQuantity(eventId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(eventId)
            return
        }

        val existingItem = cartDao.getItemByEventId(eventId) ?: return
        cartDao.insertOrUpdateItem(existingItem.copy(quantity = quantity))
    }

    override suspend fun getCart(): List<CartItem> {
        return cartDao.getCartItemsSnapshot().map { it.toDomain() }
    }
}
