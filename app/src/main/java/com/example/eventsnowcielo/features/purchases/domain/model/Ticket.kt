package com.example.eventsnowcielo.features.purchases.domain.model

data class Ticket(
    val id: Long,
    val paymentId: Long,
    val eventId: String,
    val title: String,
    val date: String,
    val time: String,
    val quantity: Int,
    val unitPriceInCents: Long,
    val totalAmountInCents: Long,
    val isPastEvent: Boolean,
)

data class Payment(
    val orderId: String?,
    val transactionId: String? = null,
    val paymentCode: String? = null,
    val email: String? = null,
    val ec: String? = null,
    val installments: Int = 1,
)

data class PaymentWithTickets(
    val payment: Payment,
    val tickets: List<Ticket>
)