package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import org.koin.core.annotation.Single

@Single(binds = [GetEventsUseCase::class])
class GetEventsUseCaseImpl(
    private val repository: EventRepository
): GetEventsUseCase {
    override suspend operator fun invoke(): Result<List<Event>> = repository.getEvents()
}
