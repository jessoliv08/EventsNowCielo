package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepository
import org.koin.core.annotation.Single

@Single
class CreateOrderUseCase(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(amount: Int, priceInCents: Long): Result<OrderModel?> {
        return repository.createOrder(amount, priceInCents)
    }
}
