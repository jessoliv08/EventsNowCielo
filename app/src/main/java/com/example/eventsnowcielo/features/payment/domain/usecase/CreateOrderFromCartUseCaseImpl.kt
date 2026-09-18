package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import org.koin.core.annotation.Single

@Single(binds = [CreateOrderFromCartUseCase::class])
class CreateOrderFromCartUseCaseImpl(
    private val repository: PaymentRepositoryImpl
): CreateOrderFromCartUseCase {
    override suspend operator fun invoke(cartItems: List<CartItem>): Result<OrderModel?> {
        return repository.createOrderFromCart(cartItems)
    }
}
