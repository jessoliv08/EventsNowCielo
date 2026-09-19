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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.eventsnowcielo.core.ui.util.formatPriceInCents
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.PaymentType
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.ui.PrintResultDialog
import com.example.eventsnowcielo.features.purchases.ui.ReceiptDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClick: () -> Unit = {},
    viewModel: PaymentViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isCartCheckout = state.checkoutTotalInCents != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cielo LIO Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
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
                MerchantSetupCard(
                    ec = state.ec,
                    email = state.email,
                    selectedQuantity = state.selectedQuantity,
                    isCartCheckout = isCartCheckout,
                    isLoading = state.isLoading,
                    hasOrder = state.createdOrder != null,
                    onEcChanged = viewModel::onEcChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onQuantitySelected = viewModel::onQuantitySelected,
                    onCreateOrder = viewModel::createOrder
                )

                state.createdOrder?.let { order ->
                    ActiveOrderCard(
                        order = order,
                        isCartCheckout = isCartCheckout,
                        checkoutTotalInCents = state.checkoutTotalInCents,
                        selectedPaymentType = state.selectedPaymentType,
                        isLoading = state.isLoading,
                        onPaymentTypeSelected = viewModel::onPaymentTypeSelected,
                        onExecuteCheckout = viewModel::executeCheckout
                    )
                }
            }

            state.paymentResult?.let { result ->
                PaymentResultDialog(
                    paymentResult = result,
                    onPrintTickets = viewModel::printTicket,
                    onShowReceipt = viewModel::showReceipt,
                    onDismiss = viewModel::dismiss
                )
            }

            state.printResult?.let {
                PrintResultDialog(
                    printResult = it,
                    onDismiss = viewModel::dismiss
                )
            }

            state.receiptMessage?.let {
                ReceiptDialog(
                    receipt = it,
                    onDismiss = viewModel::dismiss
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun MerchantSetupCard(
    ec: String,
    email: String,
    selectedQuantity: Int,
    isCartCheckout: Boolean,
    isLoading: Boolean,
    hasOrder: Boolean,
    onEcChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onQuantitySelected: (Int) -> Unit,
    onCreateOrder: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Merchant Setup", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = ec,
                onValueChange = onEcChanged,
                label = { Text("EC (Merchant Code)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                label = { Text("Customer Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isCartCheckout) {
                QuantityDropdown(
                    selectedQuantity = selectedQuantity,
                    onQuantitySelected = onQuantitySelected
                )

                Button(
                    onClick = onCreateOrder,
                    enabled = !isLoading && !hasOrder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Order")
                }
            }
        }
    }
}

@Composable
private fun QuantityDropdown(
    selectedQuantity: Int,
    onQuantitySelected: (Int) -> Unit
) {
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
                Text("Quantity: $selectedQuantity")
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
                        onQuantitySelected(qty)
                        qtyExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(
    order: OrderModel,
    isCartCheckout: Boolean,
    checkoutTotalInCents: Long?,
    selectedPaymentType: PaymentType,
    isLoading: Boolean,
    onPaymentTypeSelected: (PaymentType) -> Unit,
    onExecuteCheckout: () -> Unit
) {
    val displayTotal = checkoutTotalInCents ?: order.totalAmountInCents

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Active Order", style = MaterialTheme.typography.titleMedium)
            Text("Order ID: ${order.id}")
            if (!isCartCheckout) {
                Text("Quantity: ${order.items.sumOf { it.quantity }}")
            }
            Text("Total Amount: ${formatPriceInCents(displayTotal)}")

            PaymentTypeDropdown(
                selectedPaymentType = selectedPaymentType,
                onPaymentTypeSelected = onPaymentTypeSelected
            )

            Button(
                onClick = onExecuteCheckout,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay")
            }
        }
    }
}

@Composable
private fun PaymentTypeDropdown(
    selectedPaymentType: PaymentType,
    onPaymentTypeSelected: (PaymentType) -> Unit
) {
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
                Text("Payment: ${selectedPaymentType.label}")
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
                        onPaymentTypeSelected(type)
                        payExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentResultDialog(
    paymentResult: PaymentResult,
    onPrintTickets: (List<Ticket>) -> Unit,
    onShowReceipt: (List<Ticket>) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                Icon(
                    imageVector = paymentResult.icon,
                    contentDescription = "Payment Status Icon",
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = paymentResult.message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                if (paymentResult is PaymentResult.Success) {
                    paymentResult.tickets?.let { tickets ->
                        Button(
                            onClick = { onPrintTickets(tickets) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Print ticket")
                        }
                        Button(
                            onClick = { onShowReceipt(tickets) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show receipt")
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}