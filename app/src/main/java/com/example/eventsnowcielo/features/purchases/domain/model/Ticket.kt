package com.example.eventsnowcielo.features.purchases.domain.model

data class Ticket(
    val id: Long,
    val orderId: String,
    val eventId: String,
    val title: String,
    val date: String,
    val time: String,
    val quantity: Int,
    val unitPriceInCents: Long,
    val totalAmountInCents: Long,
    val transactionId: String?,
    val isPastEvent: Boolean
)