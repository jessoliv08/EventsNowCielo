package com.example.eventsnowcielo.features.cart.ui

import com.example.eventsnowcielo.features.cart.domain.model.CartItem

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPriceInCents: Long = 0L,
    val isProcessingPayment: Boolean = false,
    val errorMessage: String? = null,
    val paymentNavigationOrderId: String? = null,
    val paymentNavigationTotalInCents: Long? = null
) {
    val isEmpty: Boolean
        get() = items.isEmpty()
}
