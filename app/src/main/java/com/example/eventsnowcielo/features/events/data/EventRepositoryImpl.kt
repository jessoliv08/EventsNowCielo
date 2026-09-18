package com.example.eventsnowcielo.features.events.data

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import org.koin.core.annotation.Single

@Single(binds = [EventRepository::class])
class EventRepositoryImpl(
    private val remoteDataSource: EventRemoteDataSource
): EventRepository {

    private var cachedEvents: List<Event>? = null

    override suspend fun getEvents(): Result<List<Event>> {
        return runCatching {
            val events = remoteDataSource.fetchEvents()
            cachedEvents = events
            events
        }
    }

    override suspend fun getEventById(eventId: String): Result<Event> {
        return runCatching {
            val events = cachedEvents ?: remoteDataSource.fetchEvents().also { cachedEvents = it }
            events.first { it.id == eventId }
        }
    }
}
