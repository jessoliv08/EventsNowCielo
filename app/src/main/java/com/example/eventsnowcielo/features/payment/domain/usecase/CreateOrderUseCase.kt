package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.payment.domain.model.OrderModel

interface CreateOrderUseCase {
    suspend operator fun invoke(amount: Int, priceInCents: Long): Result<OrderModel?>
}
