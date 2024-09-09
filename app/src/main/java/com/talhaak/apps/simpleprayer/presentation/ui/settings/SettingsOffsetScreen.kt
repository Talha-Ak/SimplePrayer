package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.get
import com.talhaak.apps.simpleprayer.data.prayer.getOffsetLabelFor
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsOffsetScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    prayer: Prayer,
    navigateBack: () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    SettingsOffsetScreen(
        state = uiState,
        prayer = prayer,
        onConfirm = { offset ->
            settingsViewModel.updatePrayerOffset(prayer, offset)
            navigateBack()
        }
    )
}

@Composable
fun SettingsOffsetScreen(
    state: SettingsState,
    prayer: Prayer,
    onConfirm: (Int) -> Unit
) {
    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = {}
    ) {
        val offset =
            if (state is SettingsState.Success) state.prayerAdjustments[prayer]
            else 0
        OffsetPicker(
            enabled = state is SettingsState.Success,
            prayer = prayer,
            initialOffset = offset,
            methodOffset = if (state is SettingsState.Success) state.method.parameters.methodAdjustments[prayer] else 0,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun OffsetPicker(
    enabled: Boolean,
    prayer: Prayer,
    initialOffset: Int?,
    methodOffset: Int,
    onConfirm: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val offsetState = rememberPickerState(
        initialNumberOfOptions = 60 * 2 + 1,
        initiallySelectedOption = 60 + (initialOffset ?: methodOffset)
    )

    val isLargeScreen = LocalConfiguration.current.screenWidthDp >= 225
    val textStyle = if (isLargeScreen) {
        MaterialTheme.typography.display1.copy(textAlign = TextAlign.Center)
    } else {
        MaterialTheme.typography.display2.copy(textAlign = TextAlign.Center)
    }
    val paddingAroundPicker = if (isLargeScreen) 6.dp else 4.dp
    val pickerOption = pickerTextOption(textStyle) { index ->
        (index - 60).toString()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(getOffsetLabelFor(prayer)),
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.button,
            maxLines = 1,
        )
        Spacer(Modifier.height(paddingAroundPicker))
        Box(
            modifier = Modifier
                .fillMaxWidth(1 - 2 * 0.052f)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Picker(
                state = offsetState,
                onSelected = {},
                contentDescription = "test"
            ) {
                pickerOption(it, true)
            }
        }
        Spacer(Modifier.height(paddingAroundPicker))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = enabled,
                onClick = {
                    coroutineScope.launch {
                        offsetState.scrollToOption(methodOffset - 61)
                    }
                },
                colors = ButtonDefaults.secondaryButtonColors(),
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_replay_24),
                    contentDescription = stringResource(R.string.reset_to_defaults)
                )
            }
            Button(
                enabled = enabled,
                onClick = { onConfirm(offsetState.selectedOption - 60) },
                colors = ButtonDefaults.primaryButtonColors(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = stringResource(R.string.confirm)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@WearPreviewDevices
@Composable
fun SettingsOffsetScreenPreview() {
    SimplePrayerTheme {
        SettingsOffsetScreen(
            state = SettingsState.Success(
                method = CalculationMethod.MOON_SIGHTING_COMMITTEE,
                madhab = Madhab.HANAFI,
                customAngles = 18.0 to 17.0,
                prayerAdjustments = UserPrayerAdjustments(
                    null, null, null, null, null, null
                )
            ),
            prayer = Prayer.MAGHRIB,
            onConfirm = {}
        )
    }
}