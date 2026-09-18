package com.example.eventsnowcielo.features.events.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import com.example.eventsnowcielo.features.events.domain.model.EventDetailUiState
import com.example.eventsnowcielo.features.events.domain.usecase.GetEventByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EventDetailViewModel(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState(isLoading = true)

            getEventByIdUseCase(eventId)
                .onSuccess { event ->
                    _uiState.update {
                        it.copy(event = event, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Event not found."
                        )
                    }
                }
        }
    }

    fun incrementQuantity() {
        _uiState.update { it.copy(quantity = it.quantity + 1) }
    }

    fun decrementQuantity() {
        _uiState.update {
            it.copy(quantity = (it.quantity - 1).coerceAtLeast(0))
        }
    }

    fun addToCart(onAdded: () -> Unit = {}) {
        val state = _uiState.value
        val event = state.event ?: return
        if (state.quantity <= 0) return

        viewModelScope.launch {
            cartRepository.addToCart(event, state.quantity)

            _uiState.update {
                it.copy(
                    addedToCartMessage = "${state.quantity} ticket(s) added to cart.",
                    quantity = 0
                )
            }

            onAdded()
        }
    }

    fun dismissAddedToCartMessage() {
        _uiState.update { it.copy(addedToCartMessage = null) }
    }
}