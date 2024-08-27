package com.talhaak.apps.simpleprayer.presentation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.presentation.ui.PermissionRequestScreen
import com.talhaak.apps.simpleprayer.presentation.ui.prayerlist.PrayerListScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()
    val navHostState = rememberSwipeDismissableNavHostState()

    SimplePrayerTheme {
        AppScaffold(
            timeText = { ResponsiveTimeText() },
        ) {

            var permissionAttempted by rememberSaveable { mutableStateOf(false) }
            val locationPermissionState = rememberPermissionState(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) { permissionAttempted = true }

            SwipeDismissableNavHost(
                startDestination = "prayer-list",
                navController = navController,
                state = navHostState
            ) {
                composable("prayer-list") {
                    if (!locationPermissionState.status.isGranted) {
                        PermissionRequestScreen(
                            permissionState = locationPermissionState,
                            permissionAttempted = permissionAttempted,
                            message = "To calculate prayer times, location permission is needed.",
                            rationale = "You need to grant location permission to use this app",
                            chipLabel = "Allow location"
                        )
                    } else {
                        PrayerListScreen()
                    }
                }
            }

        }
    }
}