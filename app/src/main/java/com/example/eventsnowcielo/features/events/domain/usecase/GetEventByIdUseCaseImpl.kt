package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import org.koin.core.annotation.Single

@Single(binds = [GetEventByIdUseCase::class])
class GetEventByIdUseCaseImpl(
    private val repository: EventRepository
): GetEventByIdUseCase {
    override suspend operator fun invoke(eventId: String): Result<Event> = repository.getEventById(eventId)
}
