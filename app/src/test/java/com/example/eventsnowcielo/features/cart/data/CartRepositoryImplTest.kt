package com.example.eventsnowcielo.features.cart.data

import app.cash.turbine.test
import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.core.database.cart.CartItemEntity
import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CartRepositoryImplTest {

    private lateinit var cartDao: CartDao
    private lateinit var repository: CartRepositoryImpl

    private val sampleEvent = Event(
        id = "evt_101",
        title = "Rock Festival 2026",
        imageUrl = "https://example.com/image.jpg",
        date = "2026-09-17",
        time = "20:00",
        priceInCents = 15000,
        description = "High-energy rock concert",
        location = "Allianz Parque, São Paulo",
        category = Category(id = "cat_1", name = "Concerts")
    )

    @Before
    fun setUp() {
        cartDao = mockk(relaxed = true)
        every { cartDao.getCartItems() } returns flowOf(emptyList())
        repository = CartRepositoryImpl(cartDao)
    }

    @Test
    fun `cartItems should map cart dao entities to domain cart items`() = runTest {
        // Given
        val entity = sampleEvent.toCartItemEntity(quantity = 2)
        every { cartDao.getCartItems() } returns flowOf(listOf(entity))
        val repositoryWithItems = CartRepositoryImpl(cartDao)

        // When / Then
        repositoryWithItems.cartItems.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("evt_101", items[0].event.id)
            assertEquals("Rock Festival 2026", items[0].event.title)
            assertEquals(2, items[0].quantity)
            assertEquals(30000, items[0].lineTotalInCents)
            awaitComplete()
        }
    }

    @Test
    fun `addToCart should insert new item when event is not in cart`() = runTest {
        // Given
        coEvery { cartDao.getItemByEventId(sampleEvent.id) } returns null
        val entitySlot = slot<CartItemEntity>()

        // When
        repository.addToCart(sampleEvent, quantity = 1)

        // Then
        coVerify(exactly = 1) { cartDao.insertOrUpdateItem(capture(entitySlot)) }
        assertEquals("evt_101", entitySlot.captured.eventId)
        assertEquals(1, entitySlot.captured.quantity)
        assertEquals(15000, entitySlot.captured.priceInCents)
    }

    @Test
    fun `addToCart should increment quantity when event already exists in cart`() = runTest {
        // Given
        val existingEntity = sampleEvent.toCartItemEntity(quantity = 2)
        coEvery { cartDao.getItemByEventId(sampleEvent.id) } returns existingEntity
        val entitySlot = slot<CartItemEntity>()

        // When
        repository.addToCart(sampleEvent, quantity = 1)

        // Then
        coVerify(exactly = 1) { cartDao.insertOrUpdateItem(capture(entitySlot)) }
        assertEquals("evt_101", entitySlot.captured.eventId)
        assertEquals(3, entitySlot.captured.quantity)
    }

    @Test
    fun `addToCart should not insert when quantity is zero or negative`() = runTest {
        // When
        repository.addToCart(sampleEvent, quantity = 0)
        repository.addToCart(sampleEvent, quantity = -1)

        // Then
        coVerify(exactly = 0) { cartDao.getItemByEventId(any()) }
        coVerify(exactly = 0) { cartDao.insertOrUpdateItem(any()) }
    }

    @Test
    fun `removeFromCart should invoke cart dao deleteItem with event id`() = runTest {
        // When
        repository.removeFromCart("evt_101")

        // Then
        coVerify(exactly = 1) { cartDao.deleteItem("evt_101") }
    }

    @Test
    fun `clearCart should invoke cart dao clearCart`() = runTest {
        // When
        repository.clearCart()

        // Then
        coVerify(exactly = 1) { cartDao.clearCart() }
    }

    @Test
    fun `getCart should return mapped snapshot from cart dao`() = runTest {
        // Given
        val entity = sampleEvent.toCartItemEntity(quantity = 4)
        coEvery { cartDao.getCartItemsSnapshot() } returns listOf(entity)

        // When
        val cart = repository.getCart()

        // Then
        assertEquals(1, cart.size)
        assertEquals(4, cart[0].quantity)
        coVerify(exactly = 1) { cartDao.getCartItemsSnapshot() }
    }
}
