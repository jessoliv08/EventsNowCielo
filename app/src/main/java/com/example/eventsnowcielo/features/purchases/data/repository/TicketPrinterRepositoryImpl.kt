package com.example.eventsnowcielo.features.purchases.data.repository

import android.content.Context
import android.util.Log
import cielo.sdk.order.PrinterListener
import cielo.sdk.printer.PrinterManager
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.repository.TicketPrinterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [TicketPrinterRepository::class])
class TicketPrinterRepositoryImpl(context: Context): TicketPrinterRepository {

    private val printerManager: PrinterManager by lazy { PrinterManager(context.applicationContext) }

    private val _printState = MutableStateFlow<PrintResult?>(null)
    override val printState: StateFlow<PrintResult?> = _printState.asStateFlow()

    override fun printTicket(information: String, alignCenter: HashMap<String, Int>) {
        _printState.value = PrintResult.InProgress()

        val printerListener = object : PrinterListener {
            override fun onWithoutPaper() {
                Log.d("TicketPrinter", "Printer out of paper")
                _printState.value = PrintResult.OutOfPaper()
            }

            override fun onPrintSuccess() {
                Log.d("TicketPrinter", "Print successful")
                _printState.value = PrintResult.Success()
            }

            override fun onError(throwable: Throwable?) {
                val errorMsg = throwable?.message ?: "Unknown printer error"
                Log.e("TicketPrinter", "Print error: $errorMsg")
                _printState.value = PrintResult.Error(errorMessage = errorMsg)
            }
        }

        try {
            printerManager.printText(information, alignCenter, printerListener)
        } catch (e: Exception) {
            _printState.value = PrintResult.Error(errorMessage = e.message ?: "Printer initialization failed")
        }
    }

    override fun dismissResult() {
        _printState.value = null
    }
}