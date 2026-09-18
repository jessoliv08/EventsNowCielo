package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import org.koin.core.annotation.Single

@Single(binds = [CompleteOrderUseCase::class])
class CompleteOrderUseCaseImpl(
    private val repository: PaymentRepositoryImpl
): CompleteOrderUseCase {
    override suspend operator fun invoke(orderId: String, transactionId: String? ) {
        repository.completeOrder(orderId, transactionId)
    }
}
