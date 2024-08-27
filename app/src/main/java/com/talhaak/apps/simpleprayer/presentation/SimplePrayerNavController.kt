package com.talhaak.apps.simpleprayer.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavigationScreens(val route: String) {
    open val arguments: List<NamedNavArgument> = emptyList()

    object PrayerList : NavigationScreens("prayerList") {
        fun destination(): String = route
    }

    object PermissionRequest :
        NavigationScreens("permissionRequest/{type}/{message}/{rationale}/{chipLabel}/{prevScreen}") {
        const val PERMISSION_TYPE = "type"
        const val MESSAGE = "message"
        const val RATIONALE = "rationale"
        const val CHIP_LABEL = "chipLabel"
        const val PREVIOUS_SCREEN = "prevScreen"
        fun destination(
            permissionType: String,
            message: String,
            rationale: String,
            chipLabel: String,
            previousScreen: String
        ): String =
            "permissionRequest/$permissionType/$message/$rationale/$chipLabel/$previousScreen"

        override val arguments: List<NamedNavArgument>
            get() = listOf(
                navArgument(PERMISSION_TYPE) {
                    type = NavType.StringType
                },
                navArgument(MESSAGE) {
                    type = NavType.StringType
                },
                navArgument(RATIONALE) {
                    type = NavType.StringType
                },
                navArgument(CHIP_LABEL) {
                    type = NavType.StringType
                },
                navArgument(PREVIOUS_SCREEN) {
                    type = NavType.StringType
                }
            )
    }

}