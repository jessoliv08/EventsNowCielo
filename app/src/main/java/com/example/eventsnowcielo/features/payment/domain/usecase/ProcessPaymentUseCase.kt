package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import kotlinx.coroutines.flow.Flow

interface ProcessPaymentUseCase {
    operator fun invoke(
        orderId: String,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult>
}
