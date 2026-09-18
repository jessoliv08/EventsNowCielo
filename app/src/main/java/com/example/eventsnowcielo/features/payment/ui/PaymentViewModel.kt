package com.example.eventsnowcielo.features.payment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.domain.model.OrderModel
import com.example.eventsnowcielo.features.payment.domain.model.PaymentType
import com.example.eventsnowcielo.features.payment.domain.model.PaymentUiState
import com.example.eventsnowcielo.features.payment.domain.usecase.CompleteOrderUseCase
import com.example.eventsnowcielo.features.payment.domain.usecase.CreateOrderUseCase
import com.example.eventsnowcielo.features.payment.domain.usecase.ProcessPaymentUseCase
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
    private val completeOrderUseCase: CompleteOrderUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

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
                paymentStatusMessage = null,
                paymentStatusIconMessage = null
            )
        }
    }

    fun createOrder(priceInCents: Long = DEFAULT_UNIT_PRICE_IN_CENTS) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, paymentStatusMessage = null) }

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
                        paymentStatusMessage = "Failed to create order: ${error.message}"
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
                paymentStatusMessage = null,
                paymentStatusIconMessage = null,
                checkoutTotalInCents = null
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
                                paymentStatusIconMessage = status.icon,
                            )
                        }
                    }

                    is PaymentResult.Success -> {
                        completeOrderUseCase(order.id, status.transactionId)
                        cartRepository.clearCart()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentStatusIconMessage = status.icon,
                                paymentStatusMessage = status.message,
                                createdOrder = null
                            )
                        }
                    }

                    is PaymentResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentStatusMessage = "${status.message}: ${status.errorMessage}",
                                paymentStatusIconMessage = status.icon,
                            )
                        }
                    }

                    is PaymentResult.Cancelled -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentStatusMessage = status.message,
                                paymentStatusIconMessage = status.icon,
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
