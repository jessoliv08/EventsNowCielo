package com.example.eventsnowcielo.features.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.cart.domain.repository.CartRepository
import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.usecase.GetEventsUseCase
import com.example.eventsnowcielo.features.purchases.domain.repository.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EventListViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val cartRepository: CartRepository,
    purchasesRepository: PurchasesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventListUiState>(EventListUiState.Loading)
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _appliedFilters = MutableStateFlow(EventFilterState())
    val appliedFilters: StateFlow<EventFilterState> = _appliedFilters.asStateFlow()

    private val _filterDraft = MutableStateFlow(EventFilterState())
    val filterDraft: StateFlow<EventFilterState> = _filterDraft.asStateFlow()

    val completedOrdersCount: StateFlow<Int> = purchasesRepository
        .getCompletedOrdersCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    init {
        refreshEvents()
        loadCategories()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { items ->
                _cartItemCount.value = items.sumOf { it.quantity }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getEventsUseCase.getCategories()
                .onSuccess { _categories.value = it }
        }
    }

    fun syncFilterDraftFromApplied() {
        _filterDraft.value = _appliedFilters.value
    }

    fun updateFilterDraft(update: (EventFilterState) -> EventFilterState) {
        _filterDraft.update(update)
    }

    fun updateSearchQuery(query: String) {
        _appliedFilters.update { it.copy(query = query) }
        _filterDraft.update { it.copy(query = query) }
        refreshEvents()
    }

    fun applyDraftFilters() {
        _appliedFilters.value = _filterDraft.value
        refreshEvents()
    }

    fun clearFilters() {
        val cleared = EventFilterState()
        _appliedFilters.value = cleared
        _filterDraft.value = cleared
        refreshEvents()
    }

    fun loadEvents() {
        clearFilters()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            _uiState.value = EventListUiState.Loading
            getEventsUseCase.applyFilters(_appliedFilters.value)
                .handleResult()
        }
    }

    private fun Result<List<Event>>.handleResult() {
        onSuccess { events ->
            _uiState.value = when {
                events.isEmpty() -> EventListUiState.Empty
                else -> EventListUiState.Success(events)
            }
        }
        onFailure { error ->
            _uiState.value = EventListUiState.Error(
                message = error.message ?: "Unable to load events. Check your connection."
            )
        }
    }
}