package com.example.eventsnowcielo.core.ui.util

import java.util.Locale

fun formatPriceInCents(priceInCents: Long): String {
    if (priceInCents == 0L) return "Free"
    return "R$ %.2f".format(Locale("pt", "BR"), priceInCents / 100.0)
}
