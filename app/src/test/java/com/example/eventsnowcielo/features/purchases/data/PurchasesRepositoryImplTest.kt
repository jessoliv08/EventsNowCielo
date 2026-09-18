package com.example.eventsnowcielo.features.purchases.data

import app.cash.turbine.test
import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.OrderEntity
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import com.example.eventsnowcielo.features.purchases.data.repository.PurchasesRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PurchasesRepositoryImplTest {

    private lateinit var orderDao: OrderDao
    private lateinit var repository: PurchasesRepositoryImpl

    private val today = LocalDate.now()
    private val todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @Before
    fun setUp() {
        orderDao = mockk(relaxed = true)
        repository = PurchasesRepositoryImpl(orderDao)
    }

    @Test
    fun `getCompletedTickets should map entities to domain tickets with correct past flags`() = runTest {
        // Given
        val upcomingEntity = orderEntity(
            id = 1L,
            eventDate = today.plusDays(2).toString(),
            eventTitle = "Upcoming Concert"
        )
        val pastEntity = orderEntity(
            id = 2L,
            eventDate = today.minusDays(2).toString(),
            eventTitle = "Past Concert"
        )

        every {
            orderDao.getUpcomingOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
        } returns flowOf(listOf(upcomingEntity))
        every {
            orderDao.getPastOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
        } returns flowOf(listOf(pastEntity))

        // When / Then
        repository.getCompletedTickets().test {
            val tickets = awaitItem()

            assertEquals(2, tickets.size)
            assertEquals("Upcoming Concert", tickets[0].title)
            assertFalse(tickets[0].isPastEvent)
            assertEquals("Past Concert", tickets[1].title)
            assertTrue(tickets[1].isPastEvent)

            awaitComplete()
        }

        verify {
            orderDao.getUpcomingOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
            orderDao.getPastOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
        }
    }

    @Test
    fun `getCompletedTickets should place upcoming tickets before past tickets`() = runTest {
        // Given
        val upcoming = orderEntity(id = 10L, eventDate = today.plusDays(1).toString())
        val past = orderEntity(id = 20L, eventDate = today.minusDays(1).toString())

        every {
            orderDao.getUpcomingOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
        } returns flowOf(listOf(upcoming))
        every {
            orderDao.getPastOrders(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED, todayString)
        } returns flowOf(listOf(past))

        // When / Then
        repository.getCompletedTickets().test {
            val tickets = awaitItem()
            assertEquals(10L, tickets[0].id)
            assertEquals(20L, tickets[1].id)
            awaitComplete()
        }
    }

    @Test
    fun `getCompletedOrdersCount should pass through dao reactive count updates`() = runTest {
        // Given
        every {
            orderDao.getCompletedOrdersCount(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED)
        } returns flowOf(0, 3, 5)

        // When / Then
        repository.getCompletedOrdersCount().test {
            assertEquals(0, awaitItem())
            assertEquals(3, awaitItem())
            assertEquals(5, awaitItem())
            awaitComplete()
        }

        verify {
            orderDao.getCompletedOrdersCount(PaymentRepositoryImpl.ORDER_STATUS_COMPLETED)
        }
    }

    private fun orderEntity(
        id: Long,
        eventDate: String,
        eventTitle: String = "Sample Event"
    ): OrderEntity {
        return OrderEntity(
            id = id,
            orderId = "order-$id",
            eventId = "evt_$id",
            eventTitle = eventTitle,
            eventDate = eventDate,
            eventTime = "20:00",
            quantity = 1,
            unitPriceInCents = 1000,
            totalAmountInCents = 1000,
            status = PaymentRepositoryImpl.ORDER_STATUS_COMPLETED,
            transactionId = "tx-$id"
        )
    }
}
