package com.example.eventsnowcielo.features.purchases.domain.usecase

import com.example.eventsnowcielo.features.purchases.data.repository.TicketPrinterRepositoryImpl
import com.example.eventsnowcielo.features.purchases.domain.model.PrintResult
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrintTicketUseCaseImplTest {

    private lateinit var ticketPrinterManager: TicketPrinterRepositoryImpl
    private lateinit var printTicketUseCase: PrintTicketUseCaseImpl

    private val sampleTicket = Ticket(
        id = 1L,
        orderId = "order-123",
        eventId = "evt_101",
        title = "Rock Festival 2026",
        date = "2026-09-17",
        time = "20:00",
        quantity = 2,
        unitPriceInCents = 15000,
        totalAmountInCents = 30000,
        transactionId = "tx-abc",
        isPastEvent = false
    )

    @Before
    fun setUp() {
        ticketPrinterManager = mockk(relaxed = true)
        every { ticketPrinterManager.printState } returns MutableStateFlow(null)
        printTicketUseCase = PrintTicketUseCaseImpl(ticketPrinterManager)
    }

    @Test
    fun `invoke should delegate formatted text and center alignment to printer manager`() {
        // Given
        val formattedTextSlot = slot<String>()
        val alignmentSlot = slot<HashMap<String, Int>>()
        every {
            ticketPrinterManager.printTicket(capture(formattedTextSlot), capture(alignmentSlot))
        } just runs

        // When
        printTicketUseCase.printTicket(sampleTicket)

        // Then
        verify(exactly = 1) {
            ticketPrinterManager.printTicket(any(), any())
        }
        assertTrue(formattedTextSlot.captured.contains(sampleTicket.title))
        assertTrue(formattedTextSlot.captured.contains(sampleTicket.orderId))
        assertEquals(1, alignmentSlot.captured["align"])
    }

    @Test
    fun `dismissResult should delegate directly to ticket printer manager`() {
        // Given
        every { ticketPrinterManager.dismissResult() } just runs

        // When
        printTicketUseCase.dismissResult()

        // Then
        verify(exactly = 1) { ticketPrinterManager.dismissResult() }
    }

    @Test
    fun `printState should propagate print result changes from ticket printer manager`() {
        // Given
        val managerState = MutableStateFlow<PrintResult?>(null)
        every { ticketPrinterManager.printState } returns managerState
        printTicketUseCase = PrintTicketUseCaseImpl(ticketPrinterManager)

        // When
        managerState.value = PrintResult.InProgress()

        // Then
        assertEquals(PrintResult.InProgress(), printTicketUseCase.printState.value)

        // When
        managerState.value = PrintResult.Success()

        // Then
        assertEquals(PrintResult.Success(), printTicketUseCase.printState.value)

        // When
        managerState.value = PrintResult.Error(errorMessage = "Printer offline")

        // Then
        assertEquals(
            PrintResult.Error(errorMessage = "Printer offline"),
            printTicketUseCase.printState.value
        )
    }
}
