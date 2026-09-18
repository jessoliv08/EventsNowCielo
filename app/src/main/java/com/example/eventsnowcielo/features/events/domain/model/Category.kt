package com.example.eventsnowcielo.features.events.domain.model

data class Category(
    val id: String,
    val name: String
)

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val quantity: Int = 0,
    val addedToCartMessage: String? = null
)