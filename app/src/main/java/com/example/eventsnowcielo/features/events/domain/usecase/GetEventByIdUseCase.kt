package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.data.EventRepository
import com.example.eventsnowcielo.features.events.domain.model.Event
import org.koin.core.annotation.Single

@Single
class GetEventByIdUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Result<Event> = repository.getEventById(eventId)
}
