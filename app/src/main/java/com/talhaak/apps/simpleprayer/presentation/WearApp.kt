package com.talhaak.apps.simpleprayer.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.presentation.ui.PrayerListScreen

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()
    val navHostState = rememberSwipeDismissableNavHostState()

    SimplePrayerTheme {
        AppScaffold(
            timeText = { ResponsiveTimeText() },
        ) {
            SwipeDismissableNavHost(
                startDestination = "prayer-list",
                navController = navController,
                state = navHostState
            ) {
                composable("prayer-list") {
                    PrayerListScreen()
                }
            }
        }
    }
}