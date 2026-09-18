package com.example.eventsnowcielo.features.purchases.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult

@Composable
fun PrintResultDialog(
    printResult: PrintResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (printResult !is PrintResult.InProgress) {
                onDismiss()
            }
        },
        icon = {
            if (printResult is PrintResult.InProgress) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            } else {
                Icon(
                    imageVector = printResult.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when (printResult) {
                        is PrintResult.Success -> MaterialTheme.colorScheme.primary
                        is PrintResult.Error -> MaterialTheme.colorScheme.error
                        is PrintResult.OutOfPaper -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        title = {
            Text(
                text = when (printResult) {
                    is PrintResult.InProgress -> "Printing Ticket"
                    is PrintResult.Success -> "Success"
                    is PrintResult.OutOfPaper -> "Paper Required"
                    is PrintResult.Error -> "Printing Failed"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = printResult.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (printResult is PrintResult.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = printResult.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (printResult !is PrintResult.InProgress) {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        }
    )
}