package com.example.eventsnowcielo.features.payment.domain.usecase

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel

interface CreateOrderFromCartUseCase {
    suspend operator fun invoke(cartItems: List<CartItem>): Result<OrderModel?>
}
