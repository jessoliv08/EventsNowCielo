package com.example.eventsnowcielo.features.events.ui

import com.example.eventsnowcielo.features.events.domain.model.Event

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val quantity: Int = 0,
    val addedToCartMessage: String? = null
)
