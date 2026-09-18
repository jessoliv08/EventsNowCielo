package com.example.eventsnowcielo.features.events.domain.model

sealed interface EventListUiState {
    data object Loading : EventListUiState
    data class Success(val events: List<Event>) : EventListUiState
    data class Error(val message: String) : EventListUiState
    data object Empty : EventListUiState
}