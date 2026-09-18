package com.example.eventsnowcielo.features.payment.data.source

import cielo.orders.domain.CheckoutRequest
import cielo.orders.domain.Order
import cielo.sdk.order.payment.PaymentCode
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.OrderManagerConnector
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.data.repository.toOrderItemModel
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class CieloLioDataSource(
    private val connector: OrderManagerConnector
) {

    companion object {
        private const val ITEM_SKU = "2891820317391823"
        private const val ITEM_NAME = "Event Ticket"
        private const val UNIT_OF_MEASURE = "UNIDADE"
    }

    suspend fun ensureBound() {
        connector.initialize()
        connector.bind()
    }

    suspend fun createDraftOrderFromCart(cartItems: List<CartItem>): OrderModel? {
        if (cartItems.isEmpty()) return null

        ensureBound()

        val orderManager = connector.getOrderManager()
        val referenceId = UUID.randomUUID().toString()
        return orderManager.createDraftOrder(referenceId)?.let { order ->
            cartItems.forEach { cartItem ->
                order.addItem(
                    cartItem.event.id,
                    cartItem.event.title,
                    cartItem.event.priceInCents,
                    cartItem.quantity,
                    UNIT_OF_MEASURE
                )
            }

            orderManager.placeOrder(order)

            OrderModel(
                id = order.id,
                referenceId = referenceId,
                items = order.items.map { it.toOrderItemModel() }
            )
        }
    }

    suspend fun createDraftOrder(quantity: Int, unitPriceInCents: Long): OrderModel? {
        ensureBound()

        val orderManager = connector.getOrderManager()
        val referenceId = UUID.randomUUID().toString()
        return orderManager.createDraftOrder(referenceId)?.let { order ->
            order.addItem(
                ITEM_SKU,
                ITEM_NAME,
                unitPriceInCents,
                quantity,
                UNIT_OF_MEASURE
            )

            orderManager.placeOrder(order)

            OrderModel(
                id = order.id,
                referenceId = referenceId,
                items = order.items.map { it.toOrderItemModel() }
            )
        }
    }

    fun processPayment(
        orderId: String,
        amountInCents: Long,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult> = callbackFlow {
        val paymentListener = object : PaymentListener {
            override fun onStart() {
                trySend(PaymentResult.InProgress())
            }

            override fun onPayment(paidOrder: Order) {
                val transactionId = paidOrder.payments
                    .firstOrNull()
                    ?.paymentFields
                    ?.get("paymentTransactionId")
                    ?: paidOrder.id

                trySend(
                    element = PaymentResult.Success(
                        transactionId = transactionId
                    )
                )
                close()
            }

            override fun onCancel() {
                trySend(PaymentResult.Cancelled())
                close()
            }

            override fun onError(paymentError: PaymentError) {
                trySend(
                    element = PaymentResult.Error(
                        errorMessage = paymentError.description
                    )
                )
                close()
            }
        }

        val requestBuilder = CheckoutRequest.Builder()
            .orderId(orderId)
            .amount(amountInCents)

        if (email.isNotBlank()) {
            requestBuilder.email(email)
        }

        if (ec.isNotBlank()) {
            requestBuilder.ec(ec)
        }

        resolvePaymentCode(paymentCode)?.let { code ->
            requestBuilder.paymentCode(code)
        }

        connector.getOrderManager().checkoutOrder(requestBuilder.build(), paymentListener)

        awaitClose { }
    }

    fun unbind() {
        connector.unbind()
    }

    private fun resolvePaymentCode(paymentCode: String): PaymentCode? {
        return runCatching { PaymentCode.valueOf(paymentCode) }.getOrNull()
    }
}