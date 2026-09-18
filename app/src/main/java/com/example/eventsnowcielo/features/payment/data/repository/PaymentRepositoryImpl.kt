package com.example.eventsnowcielo.features.payment.data.repository

import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.OrderEntity
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.source.CieloLioDataSource
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [PaymentRepository::class])
class PaymentRepositoryImpl(
    private val dataSource: CieloLioDataSource,
    private val orderDao: OrderDao
): PaymentRepository {

    private val orderAmounts = mutableMapOf<String, Long>()

    override suspend fun createOrder(amount: Int, priceInCents: Long): Result<OrderModel?> {
        return runCatching {
            dataSource.createDraftOrder(amount, priceInCents)?.also { order ->
                cacheOrderAmount(order)
            }
        }
    }

    override suspend fun createOrderFromCart(cartItems: List<CartItem>): Result<OrderModel?> {
        return runCatching {
            dataSource.createDraftOrderFromCart(cartItems)?.also { order ->
                cacheOrderAmount(order)
                persistPendingOrders(order.id, cartItems)
            }
        }
    }

    override fun checkout(
        orderId: String,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult> {
        val amountInCents = orderAmounts[orderId]
            ?: return flow {
                emit(
                    PaymentResult.Error(
                        errorMessage = "Order amount not found for id: $orderId"
                    )
                )
            }

        return dataSource.processPayment(
            orderId = orderId,
            amountInCents = amountInCents,
            paymentCode = paymentCode,
            email = email,
            ec = ec
        )
    }

    override suspend fun completeOrder(orderId: String, transactionId: String?) {
        orderDao.updateOrderStatus(
            orderId = orderId,
            status = ORDER_STATUS_COMPLETED,
            transactionId = transactionId
        )
        orderAmounts.remove(orderId)
    }

    override fun cacheExistingOrder(order: OrderModel) {
        cacheOrderAmount(order)
    }

    private fun cacheOrderAmount(order: OrderModel) {
        orderAmounts[order.id] = order.totalAmountInCents
    }

    private suspend fun persistPendingOrders(orderId: String, cartItems: List<CartItem>) {
        val pendingOrders = cartItems.map { cartItem ->
            OrderEntity(
                orderId = orderId,
                eventId = cartItem.event.id,
                eventTitle = cartItem.event.title,
                eventDate = cartItem.event.date,
                eventTime = cartItem.event.time,
                quantity = cartItem.quantity,
                unitPriceInCents = cartItem.event.priceInCents,
                totalAmountInCents = cartItem.lineTotalInCents,
                status = ORDER_STATUS_PENDING
            )
        }
        orderDao.insertOrders(pendingOrders)
    }

    companion object {
        const val ORDER_STATUS_PENDING = "PENDING"
        const val ORDER_STATUS_COMPLETED = "COMPLETED"
    }
}
