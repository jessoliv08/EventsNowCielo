package com.example.eventsnowcielo.features.purchases.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.purchases.domain.PurchasesRepository
import com.example.eventsnowcielo.features.purchases.domain.Ticket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TicketsViewModel(
    private val purchasesRepository: PurchasesRepository
) : ViewModel() {

    private val printFeedback = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TicketsUiState> = combine(
        purchasesRepository.getCompletedTickets(),
        printFeedback
    ) { tickets, printMessage ->
        TicketsUiState(
            tickets = tickets,
            isLoading = false,
            printMessage = printMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TicketsUiState(isLoading = true)
    )

    fun printTicket(ticket: Ticket) {
        if (ticket.isPastEvent) return
        printFeedback.value = "Printing ${ticket.quantity} ticket(s) for \"${ticket.title}\"..."
    }

    fun dismissPrintMessage() {
        printFeedback.value = null
    }
}
