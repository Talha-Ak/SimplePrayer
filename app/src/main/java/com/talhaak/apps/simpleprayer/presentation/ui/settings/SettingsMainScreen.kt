package com.talhaak.apps.simpleprayer.presentation.ui.settings

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
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
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.material.Title
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.allPrayers
import com.talhaak.apps.simpleprayer.data.prayer.get
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.getOffsetLabelFor
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.presentation.ui.permissionrequest.openAppSettings

@Composable
fun SettingsMainScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    navigateToMadhabSettings: () -> Unit,
    navigateToMethodSettings: () -> Unit,
    navigateToHighLatitudeSettings: () -> Unit,
    navigateToCustomAnglesSettings: () -> Unit,
    navigateToOffsetSettings: (Prayer) -> Unit,
    navigateToBgPermissionRequest: () -> Unit,
) {
    val context = LocalContext.current
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
        navigateToOffsetSettings = navigateToOffsetSettings,
        navigateToBgPermissionRequest = navigateToBgPermissionRequest,
        navigateToDeviceSettings = { openAppSettings(context) }
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
    navigateToOffsetSettings: (Prayer) -> Unit,
    navigateToBgPermissionRequest: () -> Unit,
    navigateToDeviceSettings: () -> Unit
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(R.string.settings)
            }

            item {
                BackgroundPermissionChip(
                    onCheckedChanged = { checked ->
                        if (checked) navigateToBgPermissionRequest()
                        else navigateToDeviceSettings()
                    }
                )
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundPermissionChip(onCheckedChanged: (Boolean) -> Unit) {
    val bgLocationState = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        ToggleChip(
            checked = bgLocationState.status.isGranted,
            onCheckedChanged = onCheckedChanged,
            label = stringResource(R.string.background_location),
            toggleControl = ToggleChipToggleControl.Switch
        )
        Text(
            text = stringResource(R.string.background_location_setting_short_description),
            style = MaterialTheme.typography.caption2,
            color = if (bgLocationState.status.isGranted) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.onSurface,
            modifier = Modifier
                .listTextPadding()
                .padding(top = 8.dp)
        )
    }
}

@WearPreviewFontScales
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
            navigateToOffsetSettings = {},
            navigateToBgPermissionRequest = {},
            navigateToDeviceSettings = {}
        )
    }
}
