package com.example.eventsnowcielo.features.payment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.model.PaymentType
import com.example.eventsnowcielo.features.payment.domain.model.PaymentUiState
import com.example.eventsnowcielo.features.payment.domain.usecase.ProcessPaymentUseCase
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.domain.usecase.PrintTicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val printTicketUseCase: PrintTicketUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        // Collect printer state changes and update UI state reactively
        viewModelScope.launch {
            printTicketUseCase.printState.collect { printResult ->
                _uiState.update { it.copy(printResult = printResult) }
            }
        }
    }
    fun onEcChanged(ec: String) {
        _uiState.update { it.copy(ec = ec) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPaymentTypeSelected(paymentType: PaymentType) {

        _uiState.update {
            if (!it.isInstallment()) {
                it.copy(
                    selectedPaymentType = paymentType,
                    selectedInstallment = 1,
                )
            } else {
                it.copy(
                    selectedPaymentType = paymentType
                )
            }
        }
    }

    fun onPaymentInstallmentsSelected(installment: Int) {
        _uiState.update { it.copy(selectedInstallment = installment) }
    }

    fun initializeWithExistingOrder(orderId: String, totalInCents: Long) {
        _uiState.update {
            it.copy(
                createdOrder = OrderModel(
                    id = orderId,
                    referenceId = orderId,
                    items = emptyList()
                ),
                checkoutTotalInCents = totalInCents,
                isLoading = false,
                paymentResult = null,
            )
        }
    }

    fun dismissPrintOnly() {
        printTicketUseCase.dismissResult()
    }

    fun dismissReceiptOnly() {
        _uiState.update { currentState ->
            currentState.copy(
                receiptMessage = null
            )
        }
    }

    fun dismiss() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedPaymentType = PaymentType.CREDIT,
                createdOrder = null,
                isLoading = false,
                paymentResult = null,
                checkoutTotalInCents = null,
                printResult = null,
                receiptMessage = null,
            )
        }
        printTicketUseCase.dismissResult()
    }

    fun printTicket(tickets: List<Ticket>) {
        viewModelScope.launch {
            printTicketUseCase.printTickets(tickets)
        }
    }

    fun showReceipt(tickets: List<Ticket>) {
        _uiState.update { currentState ->
            currentState.copy(
                receiptMessage = printTicketUseCase.ticketsReceipt(tickets)
            )
        }
    }

    fun executeCheckout() {
        val state = _uiState.value
        val order = state.createdOrder ?: return

        viewModelScope.launch {
            processPaymentUseCase(
                orderId = order.id,
                paymentCode = state.selectedPaymentType.paymentCode,
                installments = state.selectedInstallment,
                email = state.email,
                ec = state.ec
            ).collect { status ->
                when (status) {
                    is PaymentResult.InProgress -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                paymentResult = status,
                            )
                        }
                    }

                    is PaymentResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentResult = status,
                                createdOrder = null
                            )
                        }
                    }

                    is PaymentResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentResult = status,
                            )
                        }
                    }

                    is PaymentResult.Cancelled -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentResult = status,
                            )
                        }
                    }
                }
            }
        }
    }
}
