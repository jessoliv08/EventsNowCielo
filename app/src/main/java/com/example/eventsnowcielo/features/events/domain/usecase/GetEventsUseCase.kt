package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event

interface GetEventsUseCase{
    suspend operator fun invoke(): Result<List<Event>>
}
