package com.example.eventsnowcielo.features.payment.data.repository

import com.example.eventsnowcielo.core.database.cart.CartDao
import com.example.eventsnowcielo.core.database.orders.OrderDao
import com.example.eventsnowcielo.core.database.orders.entity.OrderEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentWithOrders
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.source.CieloLioDataSource
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import com.example.eventsnowcielo.features.purchases.data.toTicket
import com.example.eventsnowcielo.features.purchases.domain.model.PaymentWithTickets
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
) : PaymentRepository {

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
                // 1. Update Order & Payment records in DB and retrieve updated OrderWithPayment list
                val updatedPaymentWithOrders: PaymentWithOrders? = completeOrders(
                    cieloOrderId = orderId,
                    transactionId = result.transactionId,
                    ec = ec,
                    email = email,
                    installments = installments,
                    paymentCode = paymentCode
                )

                val ticketResult: PaymentWithTickets? =
                    updatedPaymentWithOrders?.let { paymentWithOrders ->
                        PaymentWithTickets(
                            payment = paymentWithOrders.payment.toPayment(),
                            tickets = paymentWithOrders.orders.map { order ->
                                order.toTicket(LocalDate.now())
                            }
                        )
                    }

                // 2. Clear local cart state
                cartDao.clearCart()

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

    private suspend fun completeOrders(
        cieloOrderId: String,
        transactionId: String?,
        email: String?,
        ec: String?,
        installments: Int?,
        paymentCode: String?
    ): PaymentWithOrders? {
        val ordersWithPayment = orderDao.updateAndGetPaymentWithOrders(
            cieloOrderId = cieloOrderId,
            status = ORDER_STATUS_COMPLETED,
            transactionId = transactionId,
            ec = ec,
            email = email,
            installments = installments,
            paymentCode = paymentCode
        )
        orderAmounts.remove(cieloOrderId)
        return ordersWithPayment
    }

    private fun cacheOrderAmount(order: OrderModel) {
        orderAmounts[order.id] = order.totalAmountInCents
    }

    private suspend fun persistPendingOrders(cieloOrderId: String, cartItems: List<CartItem>) {
        orderDao.insertPaymentWithOrders(
            payment = PaymentEntity(cieloOrderId = cieloOrderId)
        ) { paymentId ->
            cartItems.map { cartItem ->
                OrderEntity(
                    paymentId = paymentId,
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
        }
    }

    companion object {
        const val ORDER_STATUS_PENDING = "PENDING"
        const val ORDER_STATUS_COMPLETED = "COMPLETED"
    }
}