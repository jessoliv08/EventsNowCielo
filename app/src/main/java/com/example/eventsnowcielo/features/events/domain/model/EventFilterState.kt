package com.example.eventsnowcielo.features.events.domain.model

import java.time.LocalDate

data class EventFilterState(
    val query: String = "",
    val selectedCategoryId: String? = null,
    val minPrice: Float = 0f,
    val maxPrice: Float = 500f,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isUpcomingOnly: Boolean = false,
    val isPastOnly: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
                selectedCategoryId != null ||
                minPrice > 0f ||
                maxPrice < 500f ||
                startDate != null ||
                endDate != null ||
                isUpcomingOnly ||
                isPastOnly
}