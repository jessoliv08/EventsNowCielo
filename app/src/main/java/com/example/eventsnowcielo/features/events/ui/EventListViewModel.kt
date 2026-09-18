package com.example.eventsnowcielo.features.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.events.domain.usecase.GetEventsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EventListViewModel(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventListUiState>(EventListUiState.Loading)
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventListUiState.Loading

            getEventsUseCase()
                .onSuccess { events ->
                    _uiState.value = when {
                        events.isEmpty() -> EventListUiState.Empty
                        else -> EventListUiState.Success(events)
                    }
                }
                .onFailure { error ->
                    _uiState.value = EventListUiState.Error(
                        message = error.message ?: "Unable to load events. Check your connection."
                    )
                }
        }
    }
}
