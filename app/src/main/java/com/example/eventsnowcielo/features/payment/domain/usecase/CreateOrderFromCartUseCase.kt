package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import org.koin.core.annotation.Single

@Single
class CreateOrderFromCartUseCase(
    private val repository: PaymentRepositoryImpl
) {
    suspend operator fun invoke(cartItems: List<CartItem>): Result<OrderModel?> {
        return repository.createOrderFromCart(cartItems)
    }
}
