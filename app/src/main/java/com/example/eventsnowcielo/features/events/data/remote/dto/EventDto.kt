package com.example.eventsnowcielo.features.events.data.remote.dto

import com.example.eventsnowcielo.features.events.domain.model.Event
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventDto(
    val id: String,
    val title: String,
    val imageUrl: String,
    val date: String,
    val time: String,
    val priceInCents: Long,
    val description: String,
    val location: String,
    val category: CategoryDto
)

fun EventDto.toDomain(): Event = Event(
    id = id,
    title = title,
    imageUrl = imageUrl,
    date = date,
    time = time,
    priceInCents = priceInCents,
    description = description,
    location = location,
    category = category.toDomain()
)
