package com.cmu.sweet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home_section", Icons.Filled.Home, "Início")
    object Leaderboard : BottomNavItem("leaderboard_section", Icons.Filled.Leaderboard, "Ranking")
    object Profile : BottomNavItem("profile_section", Icons.Filled.Person, "Perfil")

    // Companion object to hold the Saver
    companion object {
        /**
         * Custom Saver for BottomNavItem.
         * It saves the 'route' String of the BottomNavItem and restores the
         * correct object based on this route.
         */
        val Saver: Saver<BottomNavItem, String> = Saver(
            save = { bottomNavItem ->
                // Save the unique 'route' string associated with the item
                bottomNavItem.route
            },
            restore = { routeString ->
                // Restore the specific BottomNavItem object based on the saved route string
                when (routeString) {
                    Home.route -> Home
                    Leaderboard.route -> Leaderboard
                    Profile.route -> Profile
                    else -> throw IllegalArgumentException("Unknown route to restore BottomNavItem: $routeString")
                }
            }
        )
    }
}
