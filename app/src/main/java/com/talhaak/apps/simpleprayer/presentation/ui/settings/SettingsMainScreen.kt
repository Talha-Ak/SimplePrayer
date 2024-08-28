package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsMainScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    navigateToMadhabSettings: () -> Unit,
    navigateToMethodSettings: () -> Unit,
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text
        )
    )

    SettingsMainScreen(
        state = state,
        columnState = columnState,
        navigateToMadhabSettings = navigateToMadhabSettings,
        navigateToMethodSettings = navigateToMethodSettings
    )
}

@Composable
fun SettingsMainScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    navigateToMadhabSettings: () -> Unit,
    navigateToMethodSettings: () -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.settings)
            }

            item {
                SettingsChip(
                    label = stringResource(R.string.calculation_method),
                    secondaryLabel = if (state is SettingsState.Success) {
                        stringResource(getLabelFor(state.method))
                    } else null,
                    description = stringResource(R.string.method_setting_short_description),
                    onClick = navigateToMethodSettings
                )
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
                    label = "High Latitude Rule",
                    onClick = {}
                )
            }

            item {
                SettingsChip(
                    label = "Custom Angles",
                    onClick = {}
                )
            }

            item {
                SettingsChip(
                    label = "Custom Adjustments",
                    onClick = {}
                )
            }
        }
    }
}


@WearPreviewDevices
@Composable
fun SettingsMainScreenSuccessPreview() {
    SimplePrayerTheme {
        SettingsMainScreen(
            state = SettingsState.Success(
                madhab = Madhab.HANAFI,
                method = CalculationMethod.MOON_SIGHTING_COMMITTEE
            ),
            columnState = rememberColumnState(),
            navigateToMadhabSettings = {},
            navigateToMethodSettings = {}
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
            navigateToMadhabSettings = {},
            navigateToMethodSettings = {}
        )
    }
}

