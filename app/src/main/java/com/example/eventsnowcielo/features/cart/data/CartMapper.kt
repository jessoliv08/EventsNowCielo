package com.example.eventsnowcielo.features.cart.data

import com.example.eventsnowcielo.core.database.cart.CartItemEntity
import com.example.eventsnowcielo.features.cart.domain.model.CartItem
import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event

fun CartItemEntity.toDomain(): CartItem = CartItem(
    event = Event(
        id = eventId,
        title = title,
        imageUrl = imageUrl,
        date = date,
        time = time,
        priceInCents = priceInCents,
        description = description,
        location = location,
        category = Category(
            id = categoryId,
            name = categoryName
        )
    ),
    quantity = quantity
)

fun Event.toCartItemEntity(quantity: Int): CartItemEntity = CartItemEntity(
    eventId = id,
    title = title,
    description = description,
    priceInCents = priceInCents,
    quantity = quantity,
    imageUrl = imageUrl,
    date = date,
    time = time,
    location = location,
    categoryId = category.id,
    categoryName = category.name
)
