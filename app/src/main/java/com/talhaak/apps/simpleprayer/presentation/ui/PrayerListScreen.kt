package com.talhaak.apps.simpleprayer.presentation.ui

import android.Manifest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Card
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.presentation.Prayer
import com.talhaak.apps.simpleprayer.presentation.PrayerDay
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

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
            permissionState = locationPermissionState, permissionAttempted = permissionAttempted
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionState: PermissionState,
    permissionAttempted: Boolean,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    var show by rememberSaveable { mutableStateOf(false) }

    LocationSettingsRedirectDialog(show = show, onDismiss = { show = false })

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title("Location")
            }
            item {
                Text(
                    text = "To properly calculate prayer times, your location is needed.",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(itemPadding())
                )
            }
            item {
                Chip(label = "Update location",
                    modifier = Modifier.padding(itemPadding()),
                    onClick = {
                        if (permissionAttempted && !permissionState.status.shouldShowRationale) {
                            show = true
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    })
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

@OptIn(ExperimentalPermissionsApi::class)
@WearPreviewDevices
@Composable
fun PermissionRequestScreenPreview() {
    SimplePrayerTheme {
        PermissionRequestScreen(
            permissionState = object : PermissionState {
                override val permission = "test"
                override val status = PermissionStatus.Granted
                override fun launchPermissionRequest() {}
            }, permissionAttempted = false
        )
    }
}
