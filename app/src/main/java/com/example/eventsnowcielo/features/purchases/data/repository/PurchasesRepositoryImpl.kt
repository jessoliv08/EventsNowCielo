package com.example.eventsnowcielo.features.purchases.data.repository

import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import com.example.eventsnowcielo.features.purchases.data.toTicket
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.domain.repository.PurchasesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Single
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Single(binds = [PurchasesRepository::class])
class PurchasesRepositoryImpl(
    private val orderDao: OrderDao
) : PurchasesRepository {

    override fun getCompletedTickets(): Flow<List<Ticket>> {
        val today = todayString()
        val todayDate = LocalDate.now()

        return combine(
            orderDao.getUpcomingOrders(
                PaymentRepositoryImpl.Companion.ORDER_STATUS_COMPLETED,
                today
            ),
            orderDao.getPastOrders(PaymentRepositoryImpl.Companion.ORDER_STATUS_COMPLETED, today)
        ) { upcoming, past ->
            upcoming.map { it.toTicket(todayDate) } + past.map { it.toTicket(todayDate) }
        }
    }

    override fun getCompletedOrdersCount(): Flow<Int> {
        return orderDao.getCompletedOrdersCount(PaymentRepositoryImpl.Companion.ORDER_STATUS_COMPLETED)
    }

    private fun todayString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}