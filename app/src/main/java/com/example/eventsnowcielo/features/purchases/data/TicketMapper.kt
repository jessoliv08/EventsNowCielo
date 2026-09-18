package com.example.eventsnowcielo.features.purchases.data

import com.example.eventsnowcielo.core.database.orders.OrderEntity
import com.example.eventsnowcielo.features.purchases.domain.Ticket
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
        orderId = orderId,
        eventId = eventId,
        title = eventTitle,
        date = eventDate,
        time = eventTime,
        quantity = quantity,
        unitPriceInCents = unitPriceInCents,
        totalAmountInCents = totalAmountInCents,
        transactionId = transactionId,
        isPastEvent = isPast
    )
}