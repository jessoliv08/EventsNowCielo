package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import org.koin.core.annotation.Single

@Single(binds = [CreateOrderUseCase::class])
class CreateOrderUseCaseImpl(
    private val repository: PaymentRepositoryImpl
): CreateOrderUseCase {
    override suspend operator fun invoke(amount: Int, priceInCents: Long): Result<OrderModel?> {
        return repository.createOrder(amount, priceInCents)
    }
}
