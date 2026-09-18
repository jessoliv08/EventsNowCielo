package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.repository.PaymentRepository
import org.koin.core.annotation.Single

@Single(binds = [CreateOrderFromCartUseCase::class])
class CreateOrderFromCartUseCaseImpl(
    private val repository: PaymentRepository
): CreateOrderFromCartUseCase {
    override suspend operator fun invoke(cartItems: List<CartItem>): Result<OrderModel?> {
        return repository.createOrderFromCart(cartItems)
    }
}
