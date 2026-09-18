package com.example.eventsnowcielo.features.events.domain.repository

import com.example.eventsnowcielo.features.events.domain.model.Event

interface EventRepository {
    suspend fun getEvents(): Result<List<Event>>
    suspend fun getEventById(eventId: String): Result<Event>
}