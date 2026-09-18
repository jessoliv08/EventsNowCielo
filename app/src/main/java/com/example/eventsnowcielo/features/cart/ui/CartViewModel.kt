package com.example.eventsnowcielo.features.cart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.cart.domain.model.CartUiState
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import com.example.eventsnowcielo.features.payment.domain.usecase.CreateOrderFromCartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CartViewModel(
    private val cartRepository: CartRepository,
    private val createOrderFromCartUseCase: CreateOrderFromCartUseCase
) : ViewModel() {

    private val operationState = MutableStateFlow(CartOperationState())

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.cartItems,
        operationState
    ) { items, operation ->
        CartUiState(
            items = items,
            totalPriceInCents = items.sumOf { it.lineTotalInCents },
            isProcessingPayment = operation.isProcessingPayment,
            errorMessage = operation.errorMessage,
            paymentNavigationOrderId = operation.paymentNavigationOrderId,
            paymentNavigationTotalInCents = operation.paymentNavigationTotalInCents
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CartUiState()
    )

    fun removeFromCart(eventId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(eventId)
        }
    }

    fun payNow() {
        viewModelScope.launch {
            val cartItems = cartRepository.getCart()
            if (cartItems.isEmpty()) return@launch

            operationState.update {
                it.copy(
                    isProcessingPayment = true,
                    errorMessage = null,
                    paymentNavigationOrderId = null,
                    paymentNavigationTotalInCents = null
                )
            }

            createOrderFromCartUseCase(cartItems)
                .onSuccess { order ->
                    if (order != null) {
                        operationState.update {
                            it.copy(
                                isProcessingPayment = false,
                                paymentNavigationOrderId = order.id,
                                paymentNavigationTotalInCents = order.totalAmountInCents
                            )
                        }
                    } else {
                        operationState.update {
                            it.copy(
                                isProcessingPayment = false,
                                errorMessage = "Unable to create payment order."
                            )
                        }
                    }
                }
                .onFailure { error ->
                    operationState.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = error.message ?: "Failed to start payment."
                        )
                    }
                }
        }
    }

    fun onPaymentNavigationHandled() {
        operationState.update {
            it.copy(paymentNavigationOrderId = null, paymentNavigationTotalInCents = null)
        }
    }

    fun clearError() {
        operationState.update { it.copy(errorMessage = null) }
    }

    private data class CartOperationState(
        val isProcessingPayment: Boolean = false,
        val errorMessage: String? = null,
        val paymentNavigationOrderId: String? = null,
        val paymentNavigationTotalInCents: Long? = null
    )
}
