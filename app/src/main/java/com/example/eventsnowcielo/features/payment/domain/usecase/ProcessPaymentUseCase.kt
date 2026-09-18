package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.data.PaymentResult
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ProcessPaymentUseCase(
    private val repository: PaymentRepositoryImpl
) {
    operator fun invoke(
        orderId: String,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult> {
        return repository.checkout(orderId, paymentCode, email, ec)
    }
}
