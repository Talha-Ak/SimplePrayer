package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.Title
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsHighLatitudeScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text,
        )
    )

    SettingsHighLatitudeScreen(state, columnState, settingsViewModel::updateHighLatitudeRule)
}

@Composable
fun SettingsHighLatitudeScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    updateHighLatitudeRule: (HighLatitudeRule?) -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.high_latitude_rule)
            }
            item {
                val checked = state is SettingsState.Success && state.highLatitudeRule == null
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    ToggleChip(
                        checked = checked,
                        onCheckedChanged = { isChecked ->
                            updateHighLatitudeRule(
                                if (isChecked) null else HighLatitudeRule.MIDDLE_OF_THE_NIGHT
                            )
                        },
                        label = stringResource(R.string.high_latitude_rule_automatic),
                        toggleControl = ToggleChipToggleControl.Switch
                    )
                    Text(
                        text = stringResource(R.string.high_latitude_automatic_description),
                        style = MaterialTheme.typography.caption2,
                        color = if (checked) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .listTextPadding()
                            .padding(top = 8.dp)
                    )
                }
            }
            items(HighLatitudeRule.entries) { rule ->
                SettingsSelectableChip(
                    label = stringResource(getLabelFor(rule)),
                    secondaryLabel = stringResource(
                        when (rule) {
                            HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> R.string.middle_of_the_night_description
                            HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> R.string.seventh_of_the_night_description
                            HighLatitudeRule.TWILIGHT_ANGLE -> R.string.twilight_angle_description
                        }
                    ),
                    selected = state is SettingsState.Success && state.highLatitudeRule == rule,
                    onSelected = { updateHighLatitudeRule(rule) },
                    enabled = state is SettingsState.Success && state.highLatitudeRule != null
                )
            }
            item {
                Text(
                    text = stringResource(R.string.high_latitude_description),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(itemPadding())
                )
            }
        }
    }
}


@WearPreviewDevices
@Composable
fun SettingsHighLatitudeScreenPreview() {
    SimplePrayerTheme {
        SettingsHighLatitudeScreen(
            state = SettingsState.Success(Madhab.HANAFI, CalculationMethod.MOON_SIGHTING_COMMITTEE),
            columnState = rememberColumnState(),
            updateHighLatitudeRule = {}
        )
    }
}
