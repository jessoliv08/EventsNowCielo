package com.example.eventsnowcielo.features.purchases.data

import com.example.eventsnowcielo.core.database.orders.entity.OrderEntity
import com.example.eventsnowcielo.core.database.orders.entity.PaymentWithOrders
import com.example.eventsnowcielo.features.payment.data.repository.toPayment
import com.example.eventsnowcielo.features.purchases.domain.model.PaymentWithTickets
import com.example.eventsnowcielo.features.purchases.domain.model.Ticket
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun OrderEntity.toTicket(today: LocalDate = LocalDate.now()): Ticket {
    val isPast = runCatching {
        LocalDate.parse(eventDate, DateTimeFormatter.ISO_LOCAL_DATE).isBefore(today)
    }.getOrElse {
        // Fallback string comparison (YYYY-MM-DD format safe)
        eventDate < today.toString()
    }

    return Ticket(
        id = id,
        paymentId = paymentId,
        eventId = eventId,
        title = eventTitle,
        date = eventDate,
        time = eventTime,
        quantity = quantity,
        unitPriceInCents = unitPriceInCents,
        totalAmountInCents = totalAmountInCents,
        isPastEvent = isPast
    )
}

fun PaymentWithOrders.toPaymentWithTickets(): PaymentWithTickets {
    return PaymentWithTickets(
        payment = payment.toPayment(),
        tickets = orders.map { it.toTicket(LocalDate.now()) }
    )
}