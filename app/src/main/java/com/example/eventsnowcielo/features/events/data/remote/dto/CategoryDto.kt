package com.example.eventsnowcielo.features.events.data.remote.dto

import com.example.eventsnowcielo.features.events.domain.model.Category
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val id: String,
    val name: String
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name
)
