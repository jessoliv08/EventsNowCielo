package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import org.koin.core.annotation.Single

@Single
class CompleteOrderUseCase(
    private val repository: PaymentRepositoryImpl
) {
    suspend operator fun invoke(orderId: String, transactionId: String? = null) {
        repository.completeOrder(orderId, transactionId)
    }
}
