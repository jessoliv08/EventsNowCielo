package com.example.eventsnowcielo.features.events.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventsnowcielo.core.ui.util.formatPriceInCents
import com.example.eventsnowcielo.features.events.domain.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventFilterBottomSheet(
    filterState: EventFilterState,
    categories: List<Category>,
    onFilterStateChange: ((EventFilterState) -> EventFilterState) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter Events",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onClearFilters) {
                Text("Clear Filters")
            }
        }

        FilterSection(title = "Quick Views") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickViewChip(
                    label = "Upcoming Events",
                    selected = filterState.isUpcomingOnly,
                    onClick = {
                        onFilterStateChange { current ->
                            current.copy(
                                isUpcomingOnly = !current.isUpcomingOnly,
                                isPastOnly = false
                            )
                        }
                    }
                )
                QuickViewChip(
                    label = "Past Events",
                    selected = filterState.isPastOnly,
                    onClick = {
                        onFilterStateChange { current ->
                            current.copy(
                                isPastOnly = !current.isPastOnly,
                                isUpcomingOnly = false
                            )
                        }
                    }
                )
            }
        }

        FilterSection(title = "Category") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = filterState.selectedCategoryId == category.id
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "categoryChipColor"
                    )

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterStateChange { current ->
                                current.copy(
                                    selectedCategoryId = if (isSelected) null else category.id
                                )
                            }
                        },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = containerColor
                        )
                    )
                }
            }
        }

        FilterSection(title = "Price Range") {
            val minCents = (filterState.minPrice * 100).roundToLong()
            val maxCents = (filterState.maxPrice * 100).roundToLong()

            Text(
                text = "${formatPriceInCents(minCents)} – ${formatPriceInCents(maxCents)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RangeSlider(
                value = filterState.minPrice..filterState.maxPrice,
                onValueChange = { range ->
                    onFilterStateChange { current ->
                        current.copy(
                            minPrice = range.start,
                            maxPrice = range.endInclusive
                        )
                    }
                },
                valueRange = 0f..500f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
        }

        FilterSection(title = "Date Range") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePreset.entries.forEach { preset ->
                    val presetRange = preset.toDateRange(today)
                    val isSelected = filterState.startDate == presetRange.first &&
                            filterState.endDate == presetRange.second

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFilterStateChange { current ->
                                if (isSelected) {
                                    current.copy(startDate = null, endDate = null)
                                } else {
                                    current.copy(
                                        startDate = presetRange.first,
                                        endDate = presetRange.second
                                    )
                                }
                            }
                        },
                        label = { Text(preset.label) }
                    )
                }
            }

            if (filterState.startDate != null && filterState.endDate != null) {
                Text(
                    text = "${filterState.startDate.format(dateFormatter)} – ${filterState.endDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Button(
            onClick = onApplyFilters,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Filters")
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickViewChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

private enum class DatePreset(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    NEXT_MONTH("Next 30 Days"),
    NEXT_3_MONTHS("Next 3 Months");

    fun toDateRange(today: LocalDate): Pair<LocalDate, LocalDate> {
        return when (this) {
            THIS_WEEK -> today to today.plusDays(7)
            THIS_MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
            NEXT_MONTH -> today to today.plusDays(30)
            NEXT_3_MONTHS -> today to today.plusMonths(3)
        }
    }
}