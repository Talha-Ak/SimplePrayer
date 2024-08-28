package com.talhaak.apps.simpleprayer.presentation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.navigation.navigation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateOutOfPermissionRequest
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToPermissionRequest
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToSettings
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.presentation.ui.permissionrequest.PermissionRequestScreen
import com.talhaak.apps.simpleprayer.presentation.ui.prayerlist.PrayerListScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsScreen

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()
    val navHostState = rememberSwipeDismissableNavHostState()

    SimplePrayerTheme {
        AppScaffold(
            timeText = { ResponsiveTimeText() },
        ) {
            SwipeDismissableNavHost(
                startDestination = NavigationScreens.PrayerList.route,
                navController = navController,
                state = navHostState
            ) {
                composable(NavigationScreens.PrayerList.route) {
                    PrayerListScreen(
                        navigateToSettings = {
                            navController.navigateToSettings()
                        },
                        navigateToLocationPermissionRequest = {
                            navController.navigateToPermissionRequest(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    )
                }

                composable(
                    route = NavigationScreens.PermissionRequest.route,
                    arguments = NavigationScreens.PermissionRequest.arguments,
                ) { backstackEntry ->
                    backstackEntry.arguments?.let { args ->
                        val permission =
                            args.getString(NavigationScreens.PermissionRequest.PERMISSION_TYPE)
                        val previousScreen =
                            args.getString(NavigationScreens.PermissionRequest.PREVIOUS_SCREEN)

                        if (permission != null && previousScreen != null) {
                            PermissionRequestScreen(
                                permissionType = permission,
                                navigateOut = {
                                    navController.navigateOutOfPermissionRequest(previousScreen)
                                }
                            )
                        }
                    }
                }

                navigation(
                    route = NavigationScreens.Settings.route,
                    startDestination = NavigationScreens.Settings.Main.route
                ) {
                    composable(NavigationScreens.Settings.Main.route) {
                        SettingsScreen()
                    }

                    composable(NavigationScreens.Settings.Madhab.route) {

                    }
                }
            }
        }
    }
}