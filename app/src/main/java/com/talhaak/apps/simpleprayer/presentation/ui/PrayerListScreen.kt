package com.talhaak.apps.simpleprayer.presentation.ui

import android.Manifest
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Card
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.presentation.Prayer
import com.talhaak.apps.simpleprayer.presentation.PrayerDay
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import java.time.Instant

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerListScreen(
    prayerListViewModel: PrayerListScreenViewModel = viewModel(),
) {
    val uiState by prayerListViewModel.uiState.collectAsStateWithLifecycle()

    var permissionAttempted by rememberSaveable { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) { permissionAttempted = true }

    if (locationPermissionState.status.isGranted) {
        PrayerListScreen(uiState)
    } else {
        PermissionRequestScreen(
            permissionState = locationPermissionState,
            permissionAttempted = permissionAttempted,
            message = "To calculate prayer times, location permission is needed.",
            rationale = "You need to grant location permission to use this app",
            chipLabel = "Allow location"
        )
    }
}

@Composable
fun PrayerListScreen(uiState: PrayerListScreenState) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title("Prayer Times")
            }
            when (uiState) {
                is PrayerListScreenState.Success -> {
                    items(Prayer.entries) { prayer ->
                        Card(
                            onClick = {}, enabled = false
                        ) {
                            Row {
                                Text(text = prayer.toString())
                            }
                        }

                    }
                }

                PrayerListScreenState.Calculating -> {
                    TODO()
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PrayerListScreenPreview() {
    SimplePrayerTheme {
        PrayerListScreen(
            PrayerListScreenState.Success(
                "Shadwell", Prayer.DHUHR, 34, PrayerDay(
                    Instant.now(),
                    Instant.now().minusSeconds(3600),
                    Instant.now().minusSeconds(600),
                    Instant.now(),
                    Instant.now().plusSeconds(600),
                    Instant.now().plusSeconds(3600),
                    Instant.now().plusSeconds(7200)
                )
            )
        )
    }
}
