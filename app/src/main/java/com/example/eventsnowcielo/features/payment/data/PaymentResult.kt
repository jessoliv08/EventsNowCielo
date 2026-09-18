package com.example.eventsnowcielo.features.payment.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PaymentResult(
    open val message: String,
    open val icon: ImageVector
) {
    data class InProgress(
        override val message: String = "Payment in progress, see Cielo Card Machine",
        override val icon: ImageVector = Icons.Default.ShoppingCart
    ) : PaymentResult(message, icon)
    data class Success(
        val transactionId: String,
        override val message: String = "Yous payment in completed!",
        override val icon: ImageVector = Icons.Default.CheckCircle
    ) : PaymentResult(message, icon)
    data class Error(
        val errorMessage: String,
        override val message: String = "Sorry, but your payment couldn't be concluded",
        override val icon: ImageVector = Icons.Default.Warning
    ) : PaymentResult(message, icon)
    data class Cancelled(
        override val message: String = "The payment was canceled for some reason...",
        override val icon: ImageVector = Icons.Default.Info
    ) : PaymentResult(message, icon)
}
