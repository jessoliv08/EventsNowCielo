package com.example.eventsnowcielo.features.events.domain.model

data class Event(
    val id: String,
    val title: String,
    val imageUrl: String,
    val date: String,
    val time: String,
    val priceInCents: Long,
    val description: String,
    val location: String,
    val category: Category
)
