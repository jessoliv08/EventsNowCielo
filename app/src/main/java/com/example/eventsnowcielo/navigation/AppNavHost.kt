package com.example.eventsnowcielo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventsnowcielo.features.cart.ui.CartScreen
import com.example.eventsnowcielo.features.events.ui.EventDetailScreen
import com.example.eventsnowcielo.features.events.ui.EventListScreen
import com.example.eventsnowcielo.features.payment.ui.PaymentScreen
import com.example.eventsnowcielo.features.payment.ui.PaymentViewModel
import com.example.eventsnowcielo.features.purchases.ui.TicketsScreen
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
                },
                onTicketsClick = {
                    navController.navigate(Screen.Tickets.route)
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
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

        composable(Screen.Tickets.route) {
            TicketsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToPayment = { orderId, totalInCents ->
                    navController.navigate(Screen.Payment.createRoute(orderId, totalInCents))
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument(Screen.Payment.ORDER_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Screen.Payment.TOTAL_IN_CENTS_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString(Screen.Payment.ORDER_ID_ARG)
            val totalInCents = backStackEntry.arguments?.getLong(Screen.Payment.TOTAL_IN_CENTS_ARG) ?: 0L
            val paymentViewModel: PaymentViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)

            LaunchedEffect(orderId, totalInCents) {
                if (!orderId.isNullOrBlank() && totalInCents > 0L) {
                    paymentViewModel.initializeWithExistingOrder(orderId, totalInCents)
                }
            }

            PaymentScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = paymentViewModel
            )
        }
    }
}
