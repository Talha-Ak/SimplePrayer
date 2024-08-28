package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsCalculationMethodScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text
        )
    )

    SettingsCalculationMethodScreen(
        state = state,
        columnState = columnState,
        updateMethod = settingsViewModel::updateMethod
    )
}

@Composable
fun SettingsCalculationMethodScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    updateMethod: (CalculationMethod) -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.calculation_method)
            }
            items(CalculationMethod.entries) { method ->
                val (fajrAngle, ishaInterval, ishaAngle) = method.parameters.let {
                    Triple(it.fajrAngle, it.ishaInterval, it.ishaAngle)
                }
                SettingsSelectableChip(
                    label = stringResource(getLabelFor(method)),
                    secondaryLabel = when (ishaInterval) {
                        0 -> stringResource(R.string.fajr_isha_angles, fajrAngle, ishaAngle)
                        else -> stringResource(
                            R.string.fajr_isha_angle_interval,
                            fajrAngle,
                            ishaInterval
                        )
                    },
                    selected = state is SettingsState.Success && state.method == method,
                    onSelected = { updateMethod(method) }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.method_setting_description),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondaryVariant,
                    modifier = Modifier.padding(itemPadding())
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun SettingsCalculationMethodScreenPreview() {
    SimplePrayerTheme {
        SettingsCalculationMethodScreen(
            state = SettingsState.Success(Madhab.SHAFI, CalculationMethod.MUSLIM_WORLD_LEAGUE),
            columnState = rememberColumnState(),
            updateMethod = {}
        )
    }
}