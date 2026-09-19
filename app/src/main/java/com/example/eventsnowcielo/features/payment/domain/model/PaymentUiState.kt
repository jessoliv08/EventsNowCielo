package com.example.eventsnowcielo.features.payment.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult

data class PaymentUiState(
    val ec: String = "",
    val email: String = "",
    val selectedInstallment: Int = 1,
    val selectedPaymentType: PaymentType = PaymentType.CREDIT,
    val createdOrder: OrderModel? = null,
    val isLoading: Boolean = false,
    val paymentResult: PaymentResult? = null,
    val checkoutTotalInCents: Long? = null,
    val printResult: PrintResult? = null,
    val receiptMessage: String? = null,
) {
    fun isInstallment(): Boolean =
        selectedPaymentType == PaymentType.CREDIT_INSTALLMENT
}