package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.ui.EventFilterState
import java.time.LocalDate

interface GetEventsUseCase {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Result<List<Event>>
    suspend fun getPastEvents(today: LocalDate = LocalDate.now()): Result<List<Event>>
    suspend fun searchByNameOrLocation(query: String): Result<List<Event>>
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Result<List<Event>>
    suspend fun getByPriceRange(minPriceInCents: Long, maxPriceInCents: Long): Result<List<Event>>
    suspend fun getByCategory(categoryId: String): Result<List<Event>>
    suspend fun applyFilters(
        filters: EventFilterState,
        today: LocalDate = LocalDate.now()
    ): Result<List<Event>>
    suspend fun getCategories(): Result<List<Category>>
}
