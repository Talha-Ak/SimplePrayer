package com.talhaak.apps.simpleprayer.presentation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
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
                            navController.navigate(NavigationScreens.Settings.route)
                        },
                        navigateToLocationPermissionRequest = {
                            navController.navigate(
                                NavigationScreens.PermissionRequest.destination(
                                    permissionType = Manifest.permission.ACCESS_COARSE_LOCATION,
                                    previousScreen = NavigationScreens.PrayerList.route
                                )
                            ) {
                                popUpTo(NavigationScreens.PrayerList.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                composable(NavigationScreens.Settings.route) {
                    SettingsScreen()
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
                                    navController.navigate(previousScreen) {
                                        popUpTo(NavigationScreens.PermissionRequest.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}