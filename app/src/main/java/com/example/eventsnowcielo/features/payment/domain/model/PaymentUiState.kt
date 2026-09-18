package com.example.eventsnowcielo.features.payment.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class PaymentUiState(
    val ec: String = "",
    val email: String = "",
    val selectedQuantity: Int = 1,
    val selectedPaymentType: PaymentType = PaymentType.CREDIT,
    val createdOrder: OrderModel? = null,
    val isLoading: Boolean = false,
    val paymentStatusMessage: String? = null,
    val paymentStatusIconMessage: ImageVector? = null
)