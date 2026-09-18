package com.example.eventsnowcielo.features.events.data

import com.example.eventsnowcielo.features.events.data.remote.EventApi
import com.example.eventsnowcielo.features.events.data.remote.dto.toDomain
import com.example.eventsnowcielo.features.events.domain.model.Event
import org.koin.core.annotation.Single

@Single
class EventRemoteDataSource(
    private val eventApi: EventApi
) {
    suspend fun fetchEvents(): List<Event> {
        return eventApi.getEvents()
            .unwrapEvents()
            .map { it.toDomain() }
    }
}
