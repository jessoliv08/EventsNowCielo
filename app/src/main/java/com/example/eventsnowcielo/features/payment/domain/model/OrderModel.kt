package com.example.eventsnowcielo.features.payment.domain.model

data class OrderItemModel(
    val sku: String,
    val name: String,
    val unitPriceInCents: Int,
    val quantity: Int,
    val unitOfMeasure: String = "UNIT"
) {
    val totalItemPriceInCents: Int
        get() = unitPriceInCents * quantity
}

data class OrderModel(
    val id: String,
    val referenceId: String,
    val items: List<OrderItemModel>
) {
    val totalQuantity: Int
        get() = items.sumOf { it.quantity }

    val totalAmountInCents: Long
        get() = items.sumOf { it.totalItemPriceInCents.toLong() }
}