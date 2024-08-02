package com.talhaak.apps.simpleprayer.presentation.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.Prayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Card
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Icon
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.Title
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.talhaak.apps.simpleprayer.presentation.PrayerDay
import com.talhaak.apps.simpleprayer.presentation.label
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

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
        when (uiState) {
            is PrayerListScreenState.Success -> {
                PrayerListSuccessScreen(
                    columnState = columnState, uiState = uiState
                )
            }

            PrayerListScreenState.Calculating -> {
                Text(text = "Calculating...")
            }
        }
    }
}

@Composable
private fun PrayerListSuccessScreen(
    columnState: ScalingLazyColumnState, uiState: PrayerListScreenState.Success
) {
    ScalingLazyColumn(columnState = columnState) {
        item {
            Title("Prayer Times")
        }
        items(Prayer.entries.toTypedArray().drop(1)) { prayer ->
            if (uiState.currentPrayer == prayer) {
                CurrentPrayerCard(
                    prayer, uiState.prayers.getTimeString(prayer), uiState.currentPrayerMinutesLeft
                )
            } else {
                PrayerCard(prayer, uiState.prayers.getTimeString(prayer))
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(itemPadding()),
            ) {
                Icon(
                    paintable = Icons.Default.LocationOn.asPaintable(),
                    modifier = Modifier.size(16.dp),
                    contentDescription = "Location"
                )
                Text(
                    text = uiState.location,
                    style = MaterialTheme.typography.caption1,
                )
            }
        }
        item {
            Chip(
                label = "Update location",
                colors = ChipDefaults.outlinedChipColors(),
                onClick = {})
        }
        item {
            Chip(label = "Settings", colors = ChipDefaults.secondaryChipColors(), onClick = {})
        }
    }
}

@Composable
fun CurrentPrayerCard(prayer: Prayer, time: String, minutesLeft: String) {
    Card(
        onClick = {}, enabled = false,
        backgroundPainter = CardDefaults.cardBackgroundPainter(MaterialTheme.colors.primaryVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(prayer.label), style = MaterialTheme.typography.title3
            )
            Text(
                text = time, style = MaterialTheme.typography.title3
            )
        }
        Text(
            text = "$minutesLeft left",
            style = MaterialTheme.typography.caption3,
        )
    }
}

@Composable
fun PrayerCard(prayer: Prayer, time: String) {
    Card(
        onClick = {},
        enabled = false,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(prayer.label), style = MaterialTheme.typography.title3
            )
            Text(
                text = time, style = MaterialTheme.typography.title3
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun PrayerListScreenPreview() {
    SimplePrayerTheme {
        PrayerListScreen(
            PrayerListScreenState.Success(
                "Shadwell", Prayer.DHUHR, "34m", PrayerDay(
                    Clock.System.now(),
                    Clock.System.now() - 3600.seconds,
                    Clock.System.now() - 600.seconds,
                    Clock.System.now(),
                    Clock.System.now() + 600.seconds,
                    Clock.System.now() + 3600.seconds,
                    Clock.System.now() + 7200.seconds
                )
            )
        )
    }
}
