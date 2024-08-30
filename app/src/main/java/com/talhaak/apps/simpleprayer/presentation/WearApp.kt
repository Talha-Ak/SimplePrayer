package com.talhaak.apps.simpleprayer.presentation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navigation

import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateOutOfPermissionRequest
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToCustomAnglesSettings
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToHighLatitudeSettings
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToMadhabSettings
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToMethodSettings
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToPermissionRequest
import com.talhaak.apps.simpleprayer.presentation.SimplePrayerNavController.navigateToSettings
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.presentation.ui.permissionrequest.PermissionRequestScreen
import com.talhaak.apps.simpleprayer.presentation.ui.prayerlist.PrayerListScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsCalculationMethodScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsCustomAnglesScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsHighLatitudeScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsMadhabScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsMainScreen
import com.talhaak.apps.simpleprayer.presentation.ui.settings.SettingsSharedViewModel

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
                ) { entry ->
                    entry.arguments?.let { args ->
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
                    composable(NavigationScreens.Settings.Main.route) { entry ->
                        val viewModel = entry.sharedViewModel<SettingsSharedViewModel>(
                            navController = navController,
                            factory = SettingsSharedViewModel.Factory
                        )

                        SettingsMainScreen(
                            settingsViewModel = viewModel,
                            navigateToMadhabSettings = {
                                navController.navigateToMadhabSettings()
                            },
                            navigateToMethodSettings = {
                                navController.navigateToMethodSettings()
                            },
                            navigateToHighLatitudeSettings = {
                                navController.navigateToHighLatitudeSettings()
                            },
                            navigateToCustomAnglesSettings = {
                                navController.navigateToCustomAnglesSettings()
                            }
                        )
                    }

                    composable(NavigationScreens.Settings.Madhab.route) { entry ->
                        val viewModel = entry.sharedViewModel<SettingsSharedViewModel>(
                            navController = navController,
                            factory = SettingsSharedViewModel.Factory
                        )

                        SettingsMadhabScreen(settingsViewModel = viewModel)
                    }

                    composable(NavigationScreens.Settings.CalculationMethod.route) { entry ->
                        val viewModel = entry.sharedViewModel<SettingsSharedViewModel>(
                            navController = navController,
                            factory = SettingsSharedViewModel.Factory
                        )

                        SettingsCalculationMethodScreen(settingsViewModel = viewModel)
                    }

                    composable(NavigationScreens.Settings.HighLatitude.route) { entry ->
                        val viewModel = entry.sharedViewModel<SettingsSharedViewModel>(
                            navController = navController,
                            factory = SettingsSharedViewModel.Factory
                        )

                        SettingsHighLatitudeScreen(settingsViewModel = viewModel)
                    }

                    composable(NavigationScreens.Settings.CustomAngles.route) { entry ->
                        val viewModel = entry.sharedViewModel<SettingsSharedViewModel>(
                            navController = navController,
                            factory = SettingsSharedViewModel.Factory
                        )

                        SettingsCustomAnglesScreen(
                            settingsViewModel = viewModel,
                            navigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController,
    factory: ViewModelProvider.Factory? = null,
): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry, factory = factory)
}