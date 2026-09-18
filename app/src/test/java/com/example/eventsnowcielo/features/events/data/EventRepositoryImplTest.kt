package com.example.eventsnowcielo.features.events.data

import com.example.eventsnowcielo.features.events.data.repository.EventRepositoryImpl
import com.example.eventsnowcielo.features.events.data.source.EventRemoteDataSource
import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventRepositoryImplTest {

    private lateinit var remoteDataSource: EventRemoteDataSource
    private lateinit var repository: EventRepositoryImpl

    private val sampleEvents = listOf(
        sampleEvent(id = "evt_101", title = "Rock Festival"),
        sampleEvent(id = "evt_102", title = "Broadway Musical")
    )

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        repository = EventRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getEvents should fetch from remote datasource and map to domain models`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } returns sampleEvents

        // When
        val result = repository.getEvents()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(sampleEvents, result.getOrNull())
        coVerify(exactly = 1) { remoteDataSource.fetchEvents() }
    }

    @Test
    fun `getEvents should cache fetched events for subsequent lookups`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } returns sampleEvents

        // When
        repository.getEvents()
        val cachedLookup = repository.getEventById("evt_102")

        // Then
        assertTrue(cachedLookup.isSuccess)
        assertEquals("evt_102", cachedLookup.getOrNull()?.id)
        coVerify(exactly = 1) { remoteDataSource.fetchEvents() }
    }

    @Test
    fun `getEvents should return failure when remote datasource throws`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } throws RuntimeException("Network error")

        // When
        val result = repository.getEvents()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getEventById should return matching event when id exists`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } returns sampleEvents

        // When
        val result = repository.getEventById("evt_101")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Rock Festival", result.getOrNull()?.title)
    }

    @Test
    fun `getEventById should fetch remotely when cache is empty`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } returns sampleEvents

        // When
        val result = repository.getEventById("evt_102")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Broadway Musical", result.getOrNull()?.title)
        coVerify(exactly = 1) { remoteDataSource.fetchEvents() }
    }

    @Test
    fun `getEventById should return failure when id does not exist`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchEvents() } returns sampleEvents

        // When
        val result = repository.getEventById("evt_missing")

        // Then
        assertTrue(result.isFailure)
    }

    private fun sampleEvent(id: String, title: String): Event {
        return Event(
            id = id,
            title = title,
            imageUrl = "https://example.com/image.jpg",
            date = "2026-09-17",
            time = "20:00",
            priceInCents = 15000,
            description = "Sample description",
            location = "São Paulo",
            category = Category(id = "cat_1", name = "Concerts")
        )
    }
}
