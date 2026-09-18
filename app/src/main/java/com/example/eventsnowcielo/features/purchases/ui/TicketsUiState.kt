package com.example.eventsnowcielo.features.purchases.ui

import com.example.eventsnowcielo.features.purchases.domain.Ticket

data class TicketsUiState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
    val printMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && tickets.isEmpty()
}
