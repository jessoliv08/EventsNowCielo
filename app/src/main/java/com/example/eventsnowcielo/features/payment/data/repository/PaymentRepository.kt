package com.example.eventsnowcielo.features.payment.data.repository

import com.example.eventsnowcielo.features.payment.data.CieloLioDataSource
import com.example.eventsnowcielo.features.payment.data.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class PaymentRepository(
    private val dataSource: CieloLioDataSource
) {

    private val orderAmounts = mutableMapOf<String, Long>()

    suspend fun createOrder(amount: Int, priceInCents: Long): Result<OrderModel?> {
        return runCatching {
            dataSource.createDraftOrder(amount, priceInCents).also { order ->
                order?.let {
                    orderAmounts[order.id] = order.totalAmountInCents
                }
            }
        }
    }

    fun checkout(
        orderId: String,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult> {
        val amountInCents = orderAmounts[orderId]
            ?: return flow {
                emit(PaymentResult.Error("Order amount not found for id: $orderId"))
            }

        return dataSource.processPayment(
            orderId = orderId,
            amountInCents = amountInCents,
            paymentCode = paymentCode,
            email = email,
            ec = ec
        )
    }
}