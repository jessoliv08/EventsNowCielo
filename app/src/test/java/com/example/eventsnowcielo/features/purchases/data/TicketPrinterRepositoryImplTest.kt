package com.example.eventsnowcielo.features.purchases.data

import android.content.Context
import android.util.Log
import cielo.sdk.order.PrinterListener
import cielo.sdk.printer.PrinterManager
import com.example.eventsnowcielo.features.purchases.data.repository.TicketPrinterRepositoryImpl
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TicketPrinterRepositoryImplTest {
    private val context: Context = mockk(relaxed = true)
    private lateinit var repository: TicketPrinterRepositoryImpl

    @Before
    fun setUp() {
        // 1. Mock Android Log to prevent RuntimeException: "Method d in android.util.Log not mocked"
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        // 2. Intercept instantiation of PrinterManager inside lazy initialization
        mockkConstructor(PrinterManager::class)

        repository = TicketPrinterRepositoryImpl(context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }
    @Test
    fun `printTicket updates printState to Success when printer succeeds`() {
        val listenerSlot = slot<PrinterListener>()
        val testInfo = "Ticket Data"
        val alignMap = hashMapOf("title" to 1)

        // Intercept printerManager.printText call on the constructed instance
        every {
            anyConstructed<PrinterManager>().printText(eq(testInfo), eq(alignMap), capture(listenerSlot))
        } answers {
            listenerSlot.captured.onPrintSuccess()
        }

        repository.printTicket(testInfo, alignMap)

        assertEquals(PrintResult.Success(), repository.printState.value)
    }

    @Test
    fun `printTicket updates printState to OutOfPaper when printer is out of paper`() {
        val listenerSlot = slot<PrinterListener>()

        every {
            anyConstructed<PrinterManager>().printText(any(), any(), capture(listenerSlot))
        } answers {
            listenerSlot.captured.onWithoutPaper()
        }

        repository.printTicket("Ticket", hashMapOf())

        assertEquals(PrintResult.OutOfPaper(), repository.printState.value)
    }

    @Test
    fun `printTicket updates printState to Error when printer callback returns error`() {
        val listenerSlot = slot<PrinterListener>()
        val exception = RuntimeException("Printer hardware error")

        every {
            anyConstructed<PrinterManager>().printText(any(), any(), capture(listenerSlot))
        } answers {
            listenerSlot.captured.onError(exception)
        }

        repository.printTicket("Ticket", hashMapOf())

        assertEquals(PrintResult.Error(errorMessage = "Printer hardware error"), repository.printState.value)
    }

    @Test
    fun `printTicket handles thrown exceptions from printText`() {
        every {
            anyConstructed<PrinterManager>().printText(any(), any(), any())
        } throws RuntimeException("Fatal failure")

        repository.printTicket("Ticket", hashMapOf())

        assertEquals(PrintResult.Error(errorMessage = "Fatal failure"), repository.printState.value)
    }

    @Test
    fun `dismissResult resets state to null`() {
        val listenerSlot = slot<PrinterListener>()
        every {
            anyConstructed<PrinterManager>().printText(any(), any(), capture(listenerSlot))
        } answers {
            listenerSlot.captured.onPrintSuccess()
        }

        repository.printTicket("Ticket", hashMapOf())
        assertEquals(PrintResult.Success(), repository.printState.value)

        repository.dismissResult()
        assertNull(repository.printState.value)
    }
}