package com.example.eventsnowcielo.navigation

sealed class Screen(val route: String) {
    data object EventList : Screen("events")

    data object EventDetail : Screen("events/{eventId}") {
        fun createRoute(eventId: String): String = "events/$eventId"
    }

    data object Tickets : Screen("tickets")

    data object Cart : Screen("cart")

    data object Payment : Screen("payment?orderId={orderId}&totalInCents={totalInCents}") {
        fun createRoute(orderId: String, totalInCents: Long): String {
            return "payment?orderId=$orderId&totalInCents=$totalInCents"
        }

        const val ORDER_ID_ARG = "orderId"
        const val TOTAL_IN_CENTS_ARG = "totalInCents"
    }
}
