package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import org.koin.core.annotation.Single

@Single(binds = [CreateOrderUseCase::class])
class CreateOrderUseCaseImpl(
    private val repository: PaymentRepository
): CreateOrderUseCase {
    override suspend operator fun invoke(amount: Int, priceInCents: Long): Result<OrderModel?> {
        return repository.createOrder(amount, priceInCents)
    }
}
