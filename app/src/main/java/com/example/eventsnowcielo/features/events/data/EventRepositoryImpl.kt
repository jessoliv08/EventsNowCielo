package com.example.eventsnowcielo.features.events.data

import com.example.eventsnowcielo.features.events.domain.model.Event
import org.koin.core.annotation.Single

@Single
class EventRepository(
    private val remoteDataSource: EventRemoteDataSource
) {

    private var cachedEvents: List<Event>? = null

    suspend fun getEvents(): Result<List<Event>> {
        return runCatching {
            val events = remoteDataSource.fetchEvents()
            cachedEvents = events
            events
        }
    }

    suspend fun getEventById(eventId: String): Result<Event> {
        return runCatching {
            val events = cachedEvents ?: remoteDataSource.fetchEvents().also { cachedEvents = it }
            events.first { it.id == eventId }
        }
    }
}
