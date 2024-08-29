package com.talhaak.apps.simpleprayer.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

object SimplePrayerNavController {
    fun NavController.navigateToPermissionRequest(permission: String) {
        val prevScreen = this.currentDestination?.route ?: this.graph.startDestinationRoute ?: ""
        navigate(
            NavigationScreens.PermissionRequest.destination(permission, prevScreen)
        ) {
            popUpTo(prevScreen) {
                inclusive = true
            }
        }
    }

    fun NavController.navigateOutOfPermissionRequest(prevRoute: String) {
        navigate(prevRoute) {
            popUpTo(NavigationScreens.PermissionRequest.route) {
                inclusive = true
            }
        }
    }

    fun NavController.navigateToSettings() {
        navigate(NavigationScreens.Settings.destination())
    }

    fun NavController.navigateToMadhabSettings() {
        navigate(NavigationScreens.Settings.Madhab.destination())
    }

    fun NavController.navigateToMethodSettings() {
        navigate(NavigationScreens.Settings.CalculationMethod.destination())
    }

    fun NavController.navigateToHighLatitudeSettings() {
        navigate(NavigationScreens.Settings.HighLatitude.destination())
    }
}

sealed class NavigationScreens(val route: String) {
    open val arguments: List<NamedNavArgument> = emptyList()

    object PrayerList : NavigationScreens("prayerList") {
        fun destination(): String = route
    }

    object PermissionRequest :
        NavigationScreens("permissionRequest/{type}/{prevScreen}") {
        const val PERMISSION_TYPE = "type"
        const val PREVIOUS_SCREEN = "prevScreen"
        fun destination(
            permissionType: String,
            previousScreen: String
        ): String =
            "permissionRequest/$permissionType/$previousScreen"

        override val arguments: List<NamedNavArgument>
            get() = listOf(
                navArgument(PERMISSION_TYPE) {
                    type = NavType.StringType
                },
                navArgument(PREVIOUS_SCREEN) {
                    type = NavType.StringType
                }
            )
    }

    object Settings : NavigationScreens("settings") {
        fun destination(): String = route

        object Main : NavigationScreens("settings/main") {
            fun destination(): String = route
        }

        object Madhab : NavigationScreens("settings/madhab") {
            fun destination(): String = route
        }

        object CalculationMethod : NavigationScreens("settings/method") {
            fun destination(): String = route
        }

        object HighLatitude : NavigationScreens("settings/highLatitude") {
            fun destination(): String = route
        }
    }
}