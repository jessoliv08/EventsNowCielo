package com.example.eventsnowcielo.navigation

sealed class Screen(val route: String) {
    data object EventList : Screen("events")

    data object EventDetail : Screen("events/{eventId}") {
        fun createRoute(eventId: String): String = "events/$eventId"
    }
}
