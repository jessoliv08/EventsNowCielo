package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import com.example.eventsnowcielo.features.events.ui.EventFilterState
import org.koin.core.annotation.Single
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

@Single(binds = [GetEventsUseCase::class])
class GetEventsUseCaseImpl(
    private val repository: EventRepository
) : GetEventsUseCase {

    override suspend operator fun invoke(today: LocalDate): Result<List<Event>> {
        return repository.getEvents().map { events ->
            events.filter { event ->
                parseEventDate(event.date)?.let { !it.isBefore(today) } ?: true
            }.sortedBy { parseEventDate(it.date) ?: LocalDate.MAX }
        }
    }

    override suspend fun getPastEvents(today: LocalDate): Result<List<Event>> {
        return repository.getEvents().map { events ->
            events.filter { event ->
                parseEventDate(event.date)?.let { it.isBefore(today) } ?: false
            }.sortedBy { parseEventDate(it.date) ?: LocalDate.MAX }
        }
    }

    // 1. Search by Title or Location (LIKE query equivalent)
    override suspend fun searchByNameOrLocation(query: String): Result<List<Event>> {
        if (query.isBlank()) return invoke()

        val sanitizedQuery = query.trim().lowercase()
        return invoke().map { events ->
            events.filter { event ->
                event.title.lowercase().contains(sanitizedQuery) ||
                        event.location.lowercase().contains(sanitizedQuery)
            }
        }
    }

    // 2. Get by Date Range
    override suspend fun getByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<Event>> {
        return invoke().map { events ->
            events.filter { event ->
                parseEventDate(event.date)?.let { eventDate ->
                    (eventDate.isEqual(startDate) || eventDate.isAfter(startDate)) &&
                            (eventDate.isEqual(endDate) || eventDate.isBefore(endDate))
                } ?: false
            }
        }
    }

    // 3. Get by Price Range (inclusive)
    override suspend fun getByPriceRange(
        minPriceInCents: Long,
        maxPriceInCents: Long
    ): Result<List<Event>> {
        return invoke().map { events ->
            events.filter { event ->
                event.priceInCents in minPriceInCents..maxPriceInCents
            }
        }
    }

    // 4. Get by Category
    override suspend fun getByCategory(categoryId: String): Result<List<Event>> {
        return invoke().map { events ->
            events.filter { event ->
                event.category.id.equals(categoryId, ignoreCase = true)
            }
        }
    }
    override suspend fun applyFilters(filterState: EventFilterState, today: LocalDate): Result<List<Event>> {
        // 1. Fetch base list based on past/upcoming toggle flags
        val baseEventsResult = when {
            filterState.isPastOnly -> getPastEvents()
            else -> invoke()
        }

        // 2. Chain all active filters in memory
        return baseEventsResult.map { events ->
            events.filter { event ->
                val matchesQuery = filterState.query.isBlank() ||
                        event.title.contains(filterState.query, ignoreCase = true) ||
                        event.location.contains(filterState.query, ignoreCase = true)

                val matchesCategory = filterState.selectedCategoryId == null ||
                        event.category.id.equals(filterState.selectedCategoryId, ignoreCase = true)

                // Convert Float prices ($) to Long cents to align with event.priceInCents
                val minPriceCents = (filterState.minPrice * 100).toLong()
                val maxPriceCents = (filterState.maxPrice * 100).toLong()
                val matchesPrice = event.priceInCents in minPriceCents..maxPriceCents

                val matchesDate = if (filterState.startDate != null && filterState.endDate != null) {
                    parseEventDate(event.date)?.let { eventDate ->
                        !eventDate.isBefore(filterState.startDate) && !eventDate.isAfter(filterState.endDate)
                    } ?: false
                } else {
                    true
                }

                matchesQuery && matchesCategory && matchesPrice && matchesDate
            }
        }
    }
    override suspend fun getCategories(): Result<List<Category>> {
        return invoke().map { events ->
            events
                .map { it.category }
                .distinctBy { it.id }
                .sortedBy { it.name }
        }
    }

    private fun parseEventDate(dateString: String): LocalDate? {
        return runCatching {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()
    }
}