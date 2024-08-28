package com.talhaak.apps.simpleprayer.presentation.ui.prayerlist

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PlaceholderState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.placeholderShimmer
import androidx.wear.compose.material.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.Prayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Card
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Icon
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.OutlinedChip
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.PrayerDay
import com.talhaak.apps.simpleprayer.data.prayer.allPrayers
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.toFormattedString
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerListScreen(
    prayerListViewModel: PrayerListScreenViewModel = viewModel(factory = PrayerListScreenViewModel.Factory),
    navigateToSettings: () -> Unit,
    navigateToLocationPermissionRequest: () -> Unit
) {
    val locationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (!locationPermissionState.status.isGranted) {
        navigateToLocationPermissionRequest()
    }

    val uiState by prayerListViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    PrayerListScreen(
        uiState = uiState,
        columnState = columnState,
        navigateToSettings = navigateToSettings,
        onUpdate = prayerListViewModel::updateLocation
    )
}

@Composable
fun PrayerListScreen(
    uiState: PrayerListScreenState,
    columnState: ScalingLazyColumnState,
    navigateToSettings: () -> Unit,
    onUpdate: () -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        when (uiState) {
            is PrayerListScreenState.NoLocation -> {
                SideEffect { onUpdate() }
            }

            is PrayerListScreenState.UpdatingLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = true,
                    navigateToSettings = navigateToSettings,
                    onUpdate = onUpdate
                )
            }

            is PrayerListScreenState.FoundLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = false,
                    navigateToSettings = navigateToSettings,
                    onUpdate = onUpdate
                )
            }

            is PrayerListScreenState.FailedLocation -> {
                PrayerListFailedLocationScreen(
                    columnState = columnState,
                    onRetry = onUpdate
                )
            }
        }
    }
}

@Composable
private fun PrayerListFailedLocationScreen(
    columnState: ScalingLazyColumnState,
    onRetry: () -> Unit
) {
    ScalingLazyColumn(columnState = columnState) {
        item {
            PrayerTimesTitle()
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(itemPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    paintable = ImageVector.vectorResource(R.drawable.baseline_location_off_24)
                        .asPaintable(),
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.cant_find_location),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            LocationButton(false, onRetry)
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun PrayerListMainScreen(
    columnState: ScalingLazyColumnState,
    uiState: PrayerListScreenState.ScreenState?,
    updating: Boolean,
    navigateToSettings: () -> Unit,
    onUpdate: () -> Unit
) {
    val updatingState = rememberPlaceholderState { !updating }

    ScalingLazyColumn(columnState = columnState) {
        item {
            PrayerTimesTitle(uiState?.location ?: stringResource(R.string.updating))
        }

        when (uiState) {
            null -> items(allPrayers()) {
                PrayerCard(prayer = it, time = null, updatingState)
            }

            else -> items(allPrayers()) { prayer ->
                val time = uiState.prayers[prayer]
                when (prayer) {
                    uiState.currentPrayer -> PrayerCard(
                        prayer,
                        time,
                        updatingState,
                        CardDefaults.cardBackgroundPainter(MaterialTheme.colors.primaryVariant)
                    ) {
                        Text(
                            text = stringResource(R.string.now),
                            style = MaterialTheme.typography.caption3
                        )
                    }

                    uiState.nextPrayer?.prayer -> NextPrayerCard(
                        prayer,
                        time,
                        uiState.nextPrayer.timeTo,
                        updatingState,
                    )

                    Prayer.NONE -> Unit
                    else -> PrayerCard(
                        prayer,
                        time,
                        updatingState
                    )
                }

            }
        }

        item {
            LocationButton(updating, onUpdate)
        }

        item {
            SettingsButton(navigateToSettings)
        }
    }


    if (!updatingState.isShowContent) {
        LaunchedEffect(updatingState) {
            updatingState.startPlaceholderAnimation()
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun NextPrayerCard(
    prayer: Prayer,
    time: Instant,
    timeLeft: Duration,
    updatingState: PlaceholderState,
) {
    PrayerCard(prayer, time, updatingState) {
        Text(
            text = stringResource(R.string.next, timeLeft.inWholeMinutes.minutes),
            style = MaterialTheme.typography.caption3
        )
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun PrayerCard(
    prayer: Prayer,
    time: Instant?,
    updatingState: PlaceholderState,
    backgroundPainter: Painter = CardDefaults.cardBackgroundPainter(),
    aboveContent: @Composable () -> Unit = {}
) {
    Card(
        onClick = {},
        enabled = false,
        backgroundPainter = backgroundPainter,
        modifier = Modifier.placeholderShimmer(updatingState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                aboveContent()
                Text(
                    text = stringResource(getLabelFor(prayer)),
                    style = MaterialTheme.typography.title3
                )
            }
            if (time != null) {
                Text(
                    text = time.toFormattedString(), style = MaterialTheme.typography.title3
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(16.dp)
                        .padding(top = 1.dp, bottom = 1.dp)
                        .placeholder(updatingState)
                )
            }
        }
    }
}

@Composable
fun PrayerTimesTitle(location: String? = null) {
    ResponsiveListHeader(contentPadding = firstItemPadding()) {
        Column {
            Text(
                text = stringResource(R.string.prayer_times),
                modifier = Modifier
                    .fillMaxWidth()
                    .listTextPadding(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
            location?.let {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .listTextPadding()
                        .padding(top = 4.dp),
                ) {
                    Icon(
                        paintable = ImageVector.vectorResource(R.drawable.baseline_location_on_24)
                            .asPaintable(),
                        modifier = Modifier.size(16.dp),
                        contentDescription = stringResource(R.string.location)
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun LocationButton(updating: Boolean, onClick: () -> Unit) {
    OutlinedChip(
        label = stringResource(if (updating) R.string.updating else R.string.update_location),
        icon = ImageVector.vectorResource(R.drawable.baseline_location_on_24).asPaintable(),
        enabled = !updating,
        onClick = onClick
    )
}

@Composable
fun SettingsButton(navigateToSettings: () -> Unit) {
    Chip(
        labelId = R.string.settings,
        icon = ImageVector.vectorResource(R.drawable.baseline_settings_24).asPaintable(),
        colors = ChipDefaults.secondaryChipColors(),
        onClick = navigateToSettings
    )
}

@WearPreviewDevices
@Composable
fun PrayerListScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(),
            uiState = PrayerListScreenState.ScreenState(
                location = "Shadwell",
                currentPrayer = Prayer.DHUHR,
                nextPrayer = PrayerListScreenState.NextPrayer(Prayer.ASR, 10.minutes),
                prayers = PrayerDay(
                    Clock.System.now() - 3600.seconds,
                    Clock.System.now() - 600.seconds,
                    Clock.System.now(),
                    Clock.System.now() + 600.seconds,
                    Clock.System.now() + 3600.seconds,
                    Clock.System.now() + 7200.seconds
                )
            ),
            updating = false,
            navigateToSettings = {},
            onUpdate = {}
        )
    }
}

@WearPreviewDevices
@Composable
fun PrayerUpdatingLocationScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(), null, true, {}, {}
        )
    }
}

@WearPreviewDevices
@Composable
fun PrayerInvalidLocationScreenPreview() {
    SimplePrayerTheme {
    }
}