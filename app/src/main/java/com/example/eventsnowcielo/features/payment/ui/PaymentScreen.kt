package com.example.eventsnowcielo.features.payment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.eventsnowcielo.features.payment.domain.model.PaymentType
import org.koin.androidx.compose.koinViewModel

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // Standard non-experimental Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cielo LIO Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ){ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Merchant & Inputs
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Merchant Setup", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = state.ec,
                            onValueChange = viewModel::onEcChanged,
                            label = { Text("EC (Merchant Code)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChanged,
                            label = { Text("Customer Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Quantity Selector
                        var qtyExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { qtyExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Quantity: ${state.selectedQuantity}")
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Quantity"
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = qtyExpanded,
                                onDismissRequest = { qtyExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                (1..10).forEach { qty ->
                                    DropdownMenuItem(
                                        text = { Text("$qty") },
                                        onClick = {
                                            viewModel.onQuantitySelected(qty)
                                            qtyExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.createOrder() },
                            enabled = !state.isLoading && state.createdOrder == null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Order")
                        }
                    }
                }

                // Section 2: Payment Execution
                state.createdOrder?.let { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Active Order", style = MaterialTheme.typography.titleMedium)
                            Text("Order ID: ${order.id}")
                            Text("Quantity: ${order.items.sumOf { it.quantity }}")
                            Text("Total Amount: R$ ${"%.2f".format(order.totalAmountInCents / 100.0)}")

                            // Payment Type Selector
                            var payExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { payExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Payment: ${state.selectedPaymentType.label}")
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Payment Type"
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = payExpanded,
                                    onDismissRequest = { payExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    PaymentType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.label) },
                                            onClick = {
                                                viewModel.onPaymentTypeSelected(type)
                                                payExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = viewModel::executeCheckout,
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pay")
                            }
                        }
                    }
                }
            }
            // Section 3: Status Feedback
            state.paymentStatusMessage?.let { message ->
                Dialog(onDismissRequest = viewModel::dismiss) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            state.paymentStatusIconMessage?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Payment Status Icon",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Button(
                                onClick = viewModel::dismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            // Loading Overlay
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}