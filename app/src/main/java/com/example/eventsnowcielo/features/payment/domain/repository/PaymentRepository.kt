package com.example.eventsnowcielo.features.payment.domain.repository

import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun createOrder(amount: Int, priceInCents: Long): Result<OrderModel?>
    suspend fun createOrderFromCart(cartItems: List<CartItem>): Result<OrderModel?>
    fun checkout(
        orderId: String,
        paymentCode: String,
        email: String,
        ec: String
    ): Flow<PaymentResult>
    suspend fun completeOrder(orderId: String, transactionId: String? = null)
    fun cacheExistingOrder(order: OrderModel)
}