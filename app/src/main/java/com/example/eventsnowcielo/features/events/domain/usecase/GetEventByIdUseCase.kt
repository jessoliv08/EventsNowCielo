package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event

interface GetEventByIdUseCase {
    suspend operator fun invoke(eventId: String): Result<Pair<Event, Boolean>>
}
