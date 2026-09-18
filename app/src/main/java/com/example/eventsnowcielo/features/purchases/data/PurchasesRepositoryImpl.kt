package com.example.eventsnowcielo.features.purchases.data

import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.OrderEntity
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import com.example.eventsnowcielo.features.purchases.domain.PurchasesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Single(binds = [PurchasesRepository::class])
class PurchasesRepositoryImpl(
    private val orderDao: OrderDao
) : PurchasesRepository {

    override fun getUpcomingPurchases(): Flow<List<OrderEntity>> {
        return orderDao.getUpcomingOrders(
            status = PaymentRepositoryImpl.ORDER_STATUS_COMPLETED,
            today = todayString()
        )
    }

    override fun getPastPurchases(): Flow<List<OrderEntity>> {
        return orderDao.getPastOrders(
            status = PaymentRepositoryImpl.ORDER_STATUS_COMPLETED,
            today = todayString()
        )
    }

    private fun todayString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
