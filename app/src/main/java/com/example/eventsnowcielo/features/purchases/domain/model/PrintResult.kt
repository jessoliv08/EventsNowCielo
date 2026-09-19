package com.example.eventsnowcielo.features.purchases.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PrintResult(
    open val message: String,
    open val icon: ImageVector
) {
    data class InProgress(
        override val message: String = "Printing ticket, please wait...",
        override val icon: ImageVector = Icons.Default.Print
    ) : PrintResult(message, icon)

    data class Success(
        val resultTicket: String? = null,
        override val message: String = "Ticket printed successfully!",
        override val icon: ImageVector = Icons.Default.CheckCircle
    ) : PrintResult(message, icon)

    data class OutOfPaper(
        override val message: String = "Printer is out of paper. Please insert paper and try again.",
        override val icon: ImageVector = Icons.Default.Info
    ) : PrintResult(message, icon)

    data class Error(
        val errorMessage: String,
        override val message: String = "Failed to print ticket.",
        override val icon: ImageVector = Icons.Default.Warning
    ) : PrintResult(message, icon)
}