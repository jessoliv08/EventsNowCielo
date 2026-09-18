package com.example.eventsnowcielo.features.purchases.domain.repository

import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import kotlinx.coroutines.flow.StateFlow

interface TicketPrinterRepository {
    val printState: StateFlow<PrintResult?>
    fun printTicket(information: String, alignCenter: HashMap<String, Int>)
    fun dismissResult()
}