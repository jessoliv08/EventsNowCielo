package com.example.eventsnowcielo.features.purchases.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsnowcielo.features.purchases.domain.repository.PurchasesRepository
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import com.example.eventsnowcielo.features.purchases.domain.model.TicketsUiState
import com.example.eventsnowcielo.features.purchases.domain.usecase.PrintTicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TicketsViewModel(
    private val purchasesRepository: PurchasesRepository,
    private val printTicketUseCase: PrintTicketUseCase
) : ViewModel() {
    private val _selectedReceiptTicket = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TicketsUiState> = combine(
        purchasesRepository.getCompletedTickets(),
        printTicketUseCase.printState,
        _selectedReceiptTicket
    ) { tickets, printMessage, receipt ->
        TicketsUiState(
            tickets = tickets,
            isLoading = false,
            printResult = printMessage,
            receipt = receipt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TicketsUiState(isLoading = true)
    )

    fun printTicket(ticket: Ticket) {
        if (ticket.isPastEvent) return
        viewModelScope.launch {
            printTicketUseCase.printTicket(ticket)
        }
    }

    fun showReceipt(ticket: Ticket) {
        viewModelScope.launch {
            _selectedReceiptTicket.value = printTicketUseCase.ticketReceipt(ticket)
        }
    }

    fun dismissPrintMessage() {
        printTicketUseCase.dismissResult()
    }

    fun dismissReceipt() {
        _selectedReceiptTicket.value = null
    }
}
