package com.talhaak.apps.simpleprayer.presentation.ui.prayerlist

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

@Composable
fun PrayerListScreen(
    prayerListViewModel: PrayerListScreenViewModel = viewModel(factory = PrayerListScreenViewModel.Factory),
) {
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
        onUpdate = prayerListViewModel::updateLocation
    )
}

@Composable
fun PrayerListScreen(
    uiState: PrayerListScreenState,
    columnState: ScalingLazyColumnState,
    onUpdate: () -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        when (uiState) {
            is PrayerListScreenState.FoundLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = false,
                    onUpdate = onUpdate
                )
            }

            is PrayerListScreenState.UpdatingLocation -> {
                PrayerListMainScreen(
                    columnState = columnState,
                    uiState = uiState.state,
                    updating = true,
                    onUpdate = onUpdate
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
    onUpdate: () -> Unit
) {
    val updatingState = rememberPlaceholderState { !updating }

    ScalingLazyColumn(columnState = columnState) {
        item {
            PrayerTimesTitle(uiState?.location)
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
                            text = "Now",
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
fun NextPrayerCard(
    prayer: Prayer,
    time: Instant,
    timeLeft: Duration,
    updatingState: PlaceholderState,
) {
    PrayerCard(prayer, time, updatingState) {
        Text(
            text = "Next: ${timeLeft.inWholeMinutes.minutes}",
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
                        paintable = ImageVector.vectorResource(R.drawable.baseline_location_on_24)
                            .asPaintable(),
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
    Chip(
        label = "Settings",
        colors = ChipDefaults.secondaryChipColors(),
        onClick = {}
    )
}

@WearPreviewDevices
@Composable
fun PrayerListScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(),
            PrayerListScreenState.ScreenState(
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
            false,
        )
        {}
    }
}

@WearPreviewDevices
@Composable
fun PrayerUpdatingLocationScreenPreview() {
    SimplePrayerTheme {
        PrayerListMainScreen(
            columnState = rememberColumnState(), null, true
        ) {}
    }
}

@WearPreviewDevices
@Composable
fun PrayerInvalidLocationScreenPreview() {
    SimplePrayerTheme {
    }
}