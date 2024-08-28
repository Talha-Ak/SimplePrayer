package com.talhaak.apps.simpleprayer.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavigationScreens(val route: String) {
    open val arguments: List<NamedNavArgument> = emptyList()

    object PrayerList : NavigationScreens("prayerList") {
        fun destination(): String = route
    }

    object Settings : NavigationScreens("settings") {
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
}