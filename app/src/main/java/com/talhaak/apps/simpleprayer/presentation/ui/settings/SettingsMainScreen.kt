package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.allPrayers
import com.talhaak.apps.simpleprayer.data.prayer.get
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.getOffsetLabelFor
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsMainScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    navigateToMadhabSettings: () -> Unit,
    navigateToMethodSettings: () -> Unit,
    navigateToHighLatitudeSettings: () -> Unit,
    navigateToCustomAnglesSettings: () -> Unit,
    navigateToOffsetSettings: (Prayer) -> Unit
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
        navigateToMadhabSettings = navigateToMadhabSettings,
        navigateToMethodSettings = navigateToMethodSettings,
        navigateToHighLatitudeSettings = navigateToHighLatitudeSettings,
        navigateToCustomAnglesSettings = navigateToCustomAnglesSettings,
        navigateToOffsetSettings = navigateToOffsetSettings
    )
}

@Composable
fun SettingsMainScreen(
    state: SettingsState,
    columnState: ScalingLazyColumnState,
    navigateToMadhabSettings: () -> Unit,
    navigateToMethodSettings: () -> Unit,
    navigateToHighLatitudeSettings: () -> Unit,
    navigateToCustomAnglesSettings: () -> Unit,
    navigateToOffsetSettings: (Prayer) -> Unit
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
                    label = stringResource(R.string.high_latitude_rule),
                    secondaryLabel = if (state is SettingsState.Success) {
                        when (state.highLatitudeRule) {
                            null -> stringResource(R.string.high_latitude_rule_automatic)
                            else -> stringResource(getLabelFor(state.highLatitudeRule))
                        }
                    } else null,
                    description = stringResource(R.string.high_latitude_setting_short_description),
                    onClick = navigateToHighLatitudeSettings
                )
            }

            item {
                ResponsiveListHeader {
                    Text(stringResource(R.string.advanced))
                }
            }

            item {
                SettingsChip(
                    label = stringResource(R.string.custom_angles),
                    secondaryLabel = if (state is SettingsState.Success) {
                        val info = stringResource(
                            R.string.fajr_isha_angles,
                            state.customAngles.first ?: state.method.parameters.fajrAngle,
                            state.customAngles.second ?: state.method.parameters.ishaAngle
                        )

                        if (state.customAngles.first != null || state.customAngles.second != null) {
                            stringResource(R.string.setting_custom, info)
                        } else info
                    } else null,
                    onClick = navigateToCustomAnglesSettings
                )
            }
            items(allPrayers()) { prayer ->
                SettingsChip(
                    label = stringResource(getOffsetLabelFor(prayer)),
                    secondaryLabel = if (state is SettingsState.Success) {
                        val info = pluralStringResource(
                            R.plurals.adjustment_minutes,
                            state.prayerAdjustments[prayer]
                                ?: state.method.parameters.methodAdjustments[prayer],
                            state.prayerAdjustments[prayer]
                                ?: state.method.parameters.methodAdjustments[prayer]
                        )
                        if (state.prayerAdjustments[prayer] != null) {
                            stringResource(R.string.setting_custom, info)
                        } else info
                    } else null,
                    onClick = { navigateToOffsetSettings(prayer) }
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
                method = CalculationMethod.MOON_SIGHTING_COMMITTEE,
                customAngles = Pair(1.0, null),
                prayerAdjustments = UserPrayerAdjustments(null, null, null, null, null, null)
            ),
            columnState = rememberColumnState(),
            navigateToMadhabSettings = {},
            navigateToMethodSettings = {},
            navigateToHighLatitudeSettings = {},
            navigateToCustomAnglesSettings = {},
            navigateToOffsetSettings = {}
        )
    }
}
