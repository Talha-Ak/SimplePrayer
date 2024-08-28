package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SelectableChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
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
fun SettingsMadhabScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Text,
        )
    )

    SettingsMadhabScreen(state, columnState, settingsViewModel::updateMadhab)
}

@Composable
fun SettingsMadhabScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    updateMadhab: (Madhab) -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.madhab)
            }
            item {
                SelectableChip(
                    selected = if (state is SettingsState.Success) state.madhab == Madhab.SHAFI else false,
                    label = {
                        Text(stringResource(getLabelFor(Madhab.SHAFI)))
                    },
                    secondaryLabel = {
                        Text(stringResource(R.string.earlier_asr))
                    },
                    onClick = { selected -> if (selected) updateMadhab(Madhab.SHAFI) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SelectableChip(
                    selected = if (state is SettingsState.Success) state.madhab == Madhab.HANAFI else false,
                    label = {
                        Text(stringResource(getLabelFor(Madhab.HANAFI)))
                    },
                    secondaryLabel = {
                        Text(stringResource(R.string.later_asr))
                    },
                    onClick = { selected -> if (selected) updateMadhab(Madhab.HANAFI) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text = stringResource(R.string.madhab_setting_description),
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
fun SettingsMadhabScreenPreview() {
    SimplePrayerTheme {
        SettingsMadhabScreen(
            state = SettingsState.Success(Madhab.HANAFI),
            columnState = rememberColumnState(),
            updateMadhab = {}
        )
    }
}