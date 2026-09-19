package com.example.eventsnowcielo.features.payment.data.repository

import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.OrderEntity
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.source.CieloLioDataSource
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import com.example.eventsnowcielo.features.purchases.data.toTicket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import java.time.LocalDate

@Single(binds = [PaymentRepository::class])
class PaymentRepositoryImpl(
    private val dataSource: CieloLioDataSource,
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
): PaymentRepository {

    private val orderAmounts = mutableMapOf<String, Long>()

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
        installments: Int,
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
            installments = installments,
            email = email,
            ec = ec
        ).map { result ->
            if (result is PaymentResult.Success) {
                // 1. Update Order in DB and retrieve updated Ticket
                val updatedOrders = completeOrders(
                    orderId = orderId,
                    transactionId = result.transactionId
                )
                val ticketResult = updatedOrders.takeIf { it.isNotEmpty() }?.map {
                    it.toTicket(LocalDate.now())
                }

                // 2. Clear local cart state
                cartDao.clearCart()

                // 3. Return a NEW PaymentResult.Success holding the Ticket
                PaymentResult.Success(
                    transactionId = null,
                    tickets = ticketResult
                )
            } else {
                // Forward InProgress, Cancelled, or Error unchanged
                result
            }
        }
    }

    private suspend fun completeOrders(orderId: String, transactionId: String?): List<OrderEntity> {
        val orderEntity = orderDao.updateAndGetOrders(
            orderId = orderId,
            status = ORDER_STATUS_COMPLETED,
            transactionId = transactionId
        )
        orderAmounts.remove(orderId)
        return orderEntity
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
