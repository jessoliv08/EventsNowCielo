package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import org.koin.core.annotation.Single

@Single
class GetEventsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Result<List<Event>> = repository.getEvents()
}
