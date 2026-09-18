package com.example.eventsnowcielo.features.payment.data.repository

import cielo.orders.domain.Item
import com.example.eventsnowcielo.features.payment.domain.model.OrderItemModel

fun Item.toOrderItemModel(): OrderItemModel {
    return OrderItemModel(
        sku = this.sku,
        name = this.name,
        unitPriceInCents = this.unitPrice.toInt(),
        quantity = this.quantity,
        unitOfMeasure = this.unitOfMeasure
    )
}