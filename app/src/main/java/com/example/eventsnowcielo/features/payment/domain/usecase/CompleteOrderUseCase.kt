package com.example.eventsnowcielo.features.payment.domain.usecase

interface CompleteOrderUseCase {
    suspend operator fun invoke(orderId: String, transactionId: String? = null)
}
