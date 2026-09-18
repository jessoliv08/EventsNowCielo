package com.example.eventsnowcielo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventsnowcielo.features.events.ui.EventDetailScreen
import com.example.eventsnowcielo.features.events.ui.EventListScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.EventList.route,
        modifier = modifier
    ) {
        composable(Screen.EventList.route) {
            EventListScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable

            EventDetailScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                viewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
            )
        }
    }
}
