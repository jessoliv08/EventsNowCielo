package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import org.koin.core.annotation.Single

@Single(binds = [CompleteOrderUseCase::class])
class CompleteOrderUseCaseImpl(
    private val repository: PaymentRepository
): CompleteOrderUseCase {
    override suspend operator fun invoke(orderId: String, transactionId: String? ) {
        repository.completeOrder(orderId, transactionId)
    }
}
