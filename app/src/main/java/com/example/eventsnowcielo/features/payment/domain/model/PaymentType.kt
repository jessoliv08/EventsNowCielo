package com.example.eventsnowcielo.features.payment.domain.model

enum class PaymentType(val paymentCode: String, val label: String) {
    CREDIT("CREDITO_AVISTA", "Credit Card"),
    CREDIT_INSTALLMENT("CREDITO_PARCELADO_LOJA", "Credit Card - Installment"),
    DEBIT("DEBITO_AVISTA", "Debit Card"),
    STORE_CARD("CARTAO_LOJA_AVISTA", "Store Card"),
    STORE_CARD_INSTALLMENT("CARTAO_LOJA_PARCELADO", "Store Card - Installment"),
    OTHER("OTHER", "Other - Select on machine")
}
