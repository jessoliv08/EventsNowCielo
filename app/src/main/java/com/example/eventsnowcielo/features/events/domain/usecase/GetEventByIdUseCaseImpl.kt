package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import org.koin.core.annotation.Single
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Single(binds = [GetEventByIdUseCase::class])
class GetEventByIdUseCaseImpl(
    private val repository: EventRepository
): GetEventByIdUseCase {
    override suspend operator fun invoke(eventId: String): Result<Pair<Event, Boolean>> {
        val today = LocalDate.now()
        return repository.getEventById(eventId).map { event ->
            val isPast = parseEventDate(event.date)?.isBefore(today) ?: false
            event to isPast
        }
    }

    private fun parseEventDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            null
        }
    }}
