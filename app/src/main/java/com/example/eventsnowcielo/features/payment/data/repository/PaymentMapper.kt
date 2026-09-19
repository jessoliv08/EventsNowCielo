package com.example.eventsnowcielo.features.payment.data.repository

import cielo.orders.domain.Item
import com.example.eventsnowcielo.core.database.orders.entity.PaymentEntity
import com.example.eventsnowcielo.features.payment.domain.model.OrderItemModel
import com.example.eventsnowcielo.features.purchases.domain.model.Payment

fun Item.toOrderItemModel(): OrderItemModel {
    return OrderItemModel(
        sku = this.sku,
        name = this.name,
        unitPriceInCents = this.unitPrice.toInt(),
        quantity = this.quantity,
        unitOfMeasure = this.unitOfMeasure
    )
}

fun PaymentEntity.toPayment(): Payment {
    return Payment(
        orderId = cieloOrderId,
        transactionId = transactionId,
        paymentCode = paymentCode,
        email = email,
        ec = ec,
        installments = installments ,
    )
}