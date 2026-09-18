package com.example.eventsnowcielo.features.events.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventsResponseDto(
    val events: List<EventDto>? = null,
    val record: List<EventDto>? = null
) {
    fun unwrapEvents(): List<EventDto> = events ?: record ?: emptyList()
}
