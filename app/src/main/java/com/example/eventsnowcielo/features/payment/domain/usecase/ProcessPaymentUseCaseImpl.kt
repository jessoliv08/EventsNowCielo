package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ProcessPaymentUseCase::class])
class ProcessPaymentUseCaseImpl(
    private val repository: PaymentRepository
): ProcessPaymentUseCase {
    override operator fun invoke(
        orderId: String,
        paymentCode: String,
        installments: Int,
        email: String,
        ec: String
    ): Flow<PaymentResult> {
        return repository.checkout(orderId, paymentCode, installments, email, ec)
    }
}
