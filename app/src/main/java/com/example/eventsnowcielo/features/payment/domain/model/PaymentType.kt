package com.example.eventsnowcielo.features.payment.domain.model

enum class PaymentType(val paymentCode: String, val label: String) {
    CREDIT("CREDITO_AVISTA", "Credit Card"),
    DEBIT("DEBITO_AVISTA", "Debit Card"),
    VOUCHER("PIX", "Pix / Voucher")
}
