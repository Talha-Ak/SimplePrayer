package com.talhaak.apps.simpleprayer.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.talhaak.apps.simpleprayer.data.Prayer
import com.talhaak.apps.simpleprayer.data.PrayerDay
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerListScreen(
    prayerListViewModel: PrayerListScreenViewModel = viewModel(factory = PrayerListScreenViewModel.Factory),
) {
    val uiState by prayerListViewModel.uiState.collectAsStateWithLifecycle()
    Log.d("PrayerListScreen", "uiState: $uiState")
    PrayerListScreen(
        uiState = uiState,
        onUpdateLocation = { prayerListViewModel.updateLocation() }
        )
}

@Composable
fun PrayerListScreen(uiState: PrayerListScreenState, onUpdateLocation: () -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        when (uiState) {
            is PrayerListScreenState.FoundLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = false,
                    onUpdateLocation = onUpdateLocation
                )
            }

            is PrayerListScreenState.UpdatingLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = true,
                    onUpdateLocation = onUpdateLocation
                )
            }

            is PrayerListScreenState.FailedLocation -> {
                Text(text = "Location is failed")
            }

            is PrayerListScreenState.NoLocation -> Text(text = "Apparently No location :(")
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun PrayerListMainScreen(
    columnState: ScalingLazyColumnState,
    uiState: PrayerListScreenState.ScreenState?,
    updating: Boolean,
    onUpdateLocation: () -> Unit
) {
    val updatingState = rememberPlaceholderState { !updating }

    ScalingLazyColumn(columnState = columnState) {
        item {
            PrayerTimesTitle(uiState?.location)
        }

        when (uiState) {
            null -> items(Prayer.entries) {
                PrayerCard(prayer = it, time = null, updatingState)
            }

            else -> items(Prayer.entries) {
                val time = uiState.prayers.getTimeString(it)
                if (uiState.currentPrayer == it) {
                    CurrentPrayerCard(
                        it, time, uiState.currentPrayerMinutesLeft, updatingState
                    )
                } else {
                    PrayerCard(it, time, updatingState)
                }
            }
        }

        item {
            LocationButton(updating, onUpdateLocation)
        }

        item {
            SettingsButton()
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
fun CurrentPrayerCard(
    prayer: Prayer,
    time: String,
    minutesLeft: String,
    updatingState: PlaceholderState
) {
    Card(
        onClick = {},
        enabled = false,
        backgroundPainter = CardDefaults.cardBackgroundPainter(MaterialTheme.colors.primaryVariant),
        modifier = Modifier.placeholderShimmer(updatingState)
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

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun PrayerCard(prayer: Prayer, time: String?, updatingState: PlaceholderState) {
    Card(
        onClick = {},
        enabled = false,
        modifier = Modifier.placeholderShimmer(updatingState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(prayer.label), style = MaterialTheme.typography.title3
            )
            if (time != null) {
                Text(
                    text = time, style = MaterialTheme.typography.title3
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
fun PrayerTimesTitle(location: String?) {
    ResponsiveListHeader(contentPadding = firstItemPadding()) {
        Column {
            Text(
                text = "Prayer Times",
                modifier = Modifier
                    .fillMaxWidth()
                    .listTextPadding(),
                color = MaterialTheme.colors.onSurfaceVariant,
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
                        paintable = Icons.Default.LocationOn.asPaintable(),
                        modifier = Modifier.size(16.dp),
                        contentDescription = "Location"
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
    Chip(
        label = if (!updating) "Update location" else "Updating...",
        enabled = !updating,
        colors = ChipDefaults.outlinedChipColors(),
        onClick = onClick
    )
}

@Composable
fun SettingsButton() {
    Chip(label = "Settings", colors = ChipDefaults.secondaryChipColors(), onClick = {})
}


@WearPreviewDevices
@Composable
fun PrayerListScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(),
            PrayerListScreenState.ScreenState(
                "Shadwell", Prayer.DHUHR, "34m", PrayerDay(
                    Clock.System.now() - 3600.seconds,
                    Clock.System.now() - 600.seconds,
                    Clock.System.now(),
                    Clock.System.now() + 600.seconds,
                    Clock.System.now() + 3600.seconds,
                    Clock.System.now() + 7200.seconds
                )
            ),
            false,
            {}
        )
    }
}

@WearPreviewDevices
@Composable
fun PrayerUpdatingLocationScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(), null, true, {}
        )
    }
}
