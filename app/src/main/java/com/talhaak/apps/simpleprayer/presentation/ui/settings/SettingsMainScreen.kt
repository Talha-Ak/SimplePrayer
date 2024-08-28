package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.placeholderShimmer
import androidx.wear.compose.material.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.Madhab
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsMainScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    navigateToMadhabSettings: () -> Unit,
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    SettingsMainScreen(
        state = state,
        columnState = columnState,
        navigateToMadhabSettings = navigateToMadhabSettings
    )
}

@Composable
fun SettingsMainScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    navigateToMadhabSettings: () -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.settings)
            }
            item {
                SettingsChip(
                    label = stringResource(R.string.madhab),
                    secondaryLabel = if (state is SettingsState.Success) {
                        stringResource(getLabelFor(state.madhab))
                    } else null,
                    description = stringResource(R.string.madhab_setting_short_description),
                    onClick = navigateToMadhabSettings
                )
            }
            item {
                SettingsChip(
                    label = stringResource(R.string.calculation_method),
                    secondaryLabel = "Custom",
                    description = "Controls how prayer times are calculated.",
                    onClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SettingsChip(
    label: String,
    secondaryLabel: String? = null,
    description: String,
    onClick: () -> Unit
) {
    val loadingState = rememberPlaceholderState { !secondaryLabel.isNullOrEmpty() }
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Chip(
            label = {
                Text(text = label)
            },
            secondaryLabel = {
                if (secondaryLabel != null) {
                    Text(text = secondaryLabel)
                } else {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(16.dp)
                            .placeholder(loadingState)
                    )
                }
            },
            onClick = onClick,
            colors = ChipDefaults.secondaryChipColors(),
            modifier = Modifier
                .fillMaxWidth()
                .placeholderShimmer(loadingState)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.caption2,
            color = MaterialTheme.colors.secondaryVariant,
            modifier = Modifier.listTextPadding()
        )
    }
}

@WearPreviewDevices
@Composable
fun SettingsMainScreenSuccessPreview() {
    SimplePrayerTheme {
        SettingsMainScreen(
            state = SettingsState.Success(
                madhab = Madhab.HANAFI
            ),
            columnState = rememberColumnState(),
            navigateToMadhabSettings = {}
        )
    }
}

@WearPreviewDevices
@Composable
fun SettingsMainScreenLoadingPreview() {
    SimplePrayerTheme {
        SettingsMainScreen(
            state = SettingsState.Loading,
            columnState = rememberColumnState(),
            navigateToMadhabSettings = {}
        )
    }
}

