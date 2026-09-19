package com.example.eventsnowcielo.features.payment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.model.PaymentType
import com.example.eventsnowcielo.features.payment.domain.model.PaymentUiState
import com.example.eventsnowcielo.features.payment.domain.usecase.CreateOrderUseCase
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
    private val createOrderUseCase: CreateOrderUseCase,
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

    fun onQuantitySelected(quantity: Int) {
        _uiState.update { it.copy(selectedQuantity = quantity) }
    }

    fun onPaymentTypeSelected(paymentType: PaymentType) {
        _uiState.update { it.copy(selectedPaymentType = paymentType) }
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

    fun createOrder(priceInCents: Long = DEFAULT_UNIT_PRICE_IN_CENTS) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, paymentResult = null) }

            createOrderUseCase(
                amount = _uiState.value.selectedQuantity,
                priceInCents = priceInCents
            ).onSuccess { order ->
                _uiState.update {
                    it.copy(isLoading = false, createdOrder = order)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paymentResult = PaymentResult.Error(
                            errorMessage = "Failed to create order: ${error.message}"
                        )
                    )
                }
            }
        }
    }

    fun dismiss() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedQuantity = 1,
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
        _uiState.update { currentState ->
            currentState.copy(
                selectedQuantity = 1,
                selectedPaymentType = PaymentType.CREDIT,
                createdOrder = null,
                isLoading = false,
                paymentResult = null,
                checkoutTotalInCents = null,
                receiptMessage = null,
            )
        }
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

    companion object {
        private const val DEFAULT_UNIT_PRICE_IN_CENTS = 1000L
    }
}
