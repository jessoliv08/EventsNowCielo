package com.example.eventsnowcielo.features.payment.domain.usecase

import app.cash.turbine.test
import com.example.eventsnowcielo.features.payment.domain.model.PaymentResult
import com.example.eventsnowcielo.features.payment.data.repository.PaymentRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessPaymentUseCaseImplTest {

    private lateinit var repository: PaymentRepositoryImpl
    private lateinit var processPaymentUseCase: ProcessPaymentUseCaseImpl

    private val orderId = "order-123"
    private val paymentCode = "CREDITO_AVISTA"
    private val email = "customer@example.com"
    private val ec = "123456789"

    @Before
    fun setUp() {
        repository = mockk()
        processPaymentUseCase = ProcessPaymentUseCaseImpl(repository)
    }

    @Test
    fun `invoke should emit Success when repository checkout emits successful payment`() = runTest {
        // Given
        val transactionId = "tx-success-001"
        every {
            repository.checkout(orderId, paymentCode, email, ec)
        } returns flowOf(
            PaymentResult.InProgress(),
            PaymentResult.Success(transactionId = transactionId)
        )

        // When / Then
        processPaymentUseCase(orderId, paymentCode, email, ec).test {
            assertTrue(awaitItem() is PaymentResult.InProgress)

            val success = awaitItem()
            assertTrue(success is PaymentResult.Success)
            assertEquals(transactionId, (success as PaymentResult.Success).transactionId)

            awaitComplete()
        }

        verify(exactly = 1) {
            repository.checkout(orderId, paymentCode, email, ec)
        }
    }

    @Test
    fun `invoke should emit Error when repository checkout emits payment error`() = runTest {
        // Given
        every {
            repository.checkout(orderId, paymentCode, email, ec)
        } returns flowOf(
            PaymentResult.InProgress(),
            PaymentResult.Error(errorMessage = "SDK connection failed")
        )

        // When / Then
        processPaymentUseCase(orderId, paymentCode, email, ec).test {
            assertTrue(awaitItem() is PaymentResult.InProgress)

            val error = awaitItem()
            assertTrue(error is PaymentResult.Error)
            assertEquals(
                "SDK connection failed",
                (error as PaymentResult.Error).errorMessage
            )

            awaitComplete()
        }

        verify(exactly = 1) {
            repository.checkout(orderId, paymentCode, email, ec)
        }
    }

    @Test
    fun `invoke should emit Cancelled when repository checkout emits cancellation`() = runTest {
        // Given
        every {
            repository.checkout(orderId, paymentCode, email, ec)
        } returns flowOf(
            PaymentResult.InProgress(),
            PaymentResult.Cancelled()
        )

        // When / Then
        processPaymentUseCase(orderId, paymentCode, email, ec).test {
            assertTrue(awaitItem() is PaymentResult.InProgress)
            assertTrue(awaitItem() is PaymentResult.Cancelled)
            awaitComplete()
        }

        verify(exactly = 1) {
            repository.checkout(orderId, paymentCode, email, ec)
        }
    }

    @Test
    fun `invoke should delegate checkout parameters unchanged to repository`() = runTest {
        // Given
        every {
            repository.checkout(orderId, paymentCode, email, ec)
        } returns flowOf(PaymentResult.Success(transactionId = "tx-1"))

        // When
        processPaymentUseCase(orderId, paymentCode, email, ec).test {
            awaitItem()
            awaitComplete()
        }

        // Then
        verify(exactly = 1) {
            repository.checkout(
                orderId = orderId,
                paymentCode = paymentCode,
                email = email,
                ec = ec
            )
        }
    }
}
