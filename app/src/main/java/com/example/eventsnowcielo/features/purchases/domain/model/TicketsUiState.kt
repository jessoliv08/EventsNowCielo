package com.example.eventsnowcielo.features.purchases.domain.model

data class TicketsUiState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
    val printResult: PrintResult? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && tickets.isEmpty()
}