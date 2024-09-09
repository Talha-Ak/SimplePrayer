package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PickerGroup
import androidx.wear.compose.material.PickerGroupItem
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerGroupState
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsCustomAnglesScreen(
    settingsViewModel: SettingsSharedViewModel = viewModel(factory = SettingsSharedViewModel.Factory),
    navigateBack: () -> Unit
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    SettingsCustomAnglesScreen(
        state = state,
        onConfirm = { fajr, isha ->
            settingsViewModel.updateCustomAngles(fajr, isha)
            navigateBack()
        }
    )
}

@Composable
fun SettingsCustomAnglesScreen(
    state: SettingsState,
    onConfirm: (Double, Double) -> Unit
) {

    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = {}
    ) {
        if (state is SettingsState.Success) {
            val initialAngles = state.customAngles
            val methodAngles =
                state.method.parameters.fajrAngle to state.method.parameters.ishaAngle
            FajrIshaAnglePicker(
                initialAngles = initialAngles,
                methodAngles = methodAngles,
                ishaBlocked = state.method == CalculationMethod.QATAR,
                onConfirm = onConfirm
            )
        } else {
            FajrIshaAnglePicker(enabled = false)
        }
    }
}

@Composable
fun FajrIshaAnglePicker(
    enabled: Boolean = true,
    initialAngles: Pair<Double?, Double?> = 0.0 to 0.0,
    methodAngles: Pair<Double, Double> = 0.0 to 0.0,
    ishaBlocked: Boolean = false,
    onConfirm: (Double, Double) -> Unit = { _, _ -> },
) {
    val coroutineScope = rememberCoroutineScope()
    val fajrAngleState = rememberPickerState(
        initialNumberOfOptions = (1 + (20.0 - 9.0) * 2).toInt(),
        initiallySelectedOption = angleToIndex(initialAngles.first ?: methodAngles.first),
    )
    val ishaAngleState = rememberPickerState(
        initialNumberOfOptions = if (ishaBlocked) 1 else (1 + (20.0 - 9.0) * 2).toInt(),
        initiallySelectedOption = if (ishaBlocked) 0 else angleToIndex(
            initialAngles.second ?: methodAngles.second
        ),
        repeatItems = !ishaBlocked
    )
    val pickerGroupState = rememberPickerGroupState(FocusableElementsPicker.FAJR.index)

    val isLargeScreen = LocalConfiguration.current.screenWidthDp >= 225
    val paddingAroundPicker = if (isLargeScreen) 6.dp else 4.dp
    val textStyle = if (isLargeScreen) {
        MaterialTheme.typography.display2.copy(textAlign = TextAlign.Center)
    } else {
        MaterialTheme.typography.display3.copy(textAlign = TextAlign.Center)
    }

    val focusRequesterResetButton = remember { FocusRequester() }
    val focusRequesterConfirmButton = remember { FocusRequester() }

    val pickerWidth = getPickerWidth(textStyle)
    val pickerFajrOption = pickerTextOption(textStyle) { "%2.1f°".format(indexToAngle(it)) }
    val pickerIshaOption = pickerTextOption(textStyle) {
        "%2.1f°".format(
            if (ishaBlocked) methodAngles.second else indexToAngle(it)
        )
    }
    val onPickerSelected =
        { current: FocusableElementsPicker, next: FocusableElementsPicker ->
            if (pickerGroupState.selectedIndex != current.index) {
                pickerGroupState.selectedIndex = current.index
            } else {
                pickerGroupState.selectedIndex = next.index
                if (next == FocusableElementsPicker.CONFIRM_BUTTON) {
                    focusRequesterConfirmButton.requestFocus()
                }
            }
        }

    val pickerGroupItems = mutableListOf(
        PickerGroupItem(
            pickerState = fajrAngleState,
            modifier = Modifier
                .width(pickerWidth)
                .fillMaxHeight(),
            onSelected = {
                onPickerSelected(
                    FocusableElementsPicker.FAJR,
                    FocusableElementsPicker.ISHA,
                )
            },
            option = pickerFajrOption,
        ),
        PickerGroupItem(
            pickerState = ishaAngleState,
            modifier = Modifier
                .width(pickerWidth)
                .fillMaxHeight(),
            onSelected = {
                onPickerSelected(
                    FocusableElementsPicker.ISHA,
                    FocusableElementsPicker.RESET_BUTTON
                )
            },
            option = pickerIshaOption,
        ),
    )

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Text(
            text = when (FocusableElementsPicker[pickerGroupState.selectedIndex]) {
                FocusableElementsPicker.FAJR -> stringResource(R.string.fajr_angle)
                FocusableElementsPicker.ISHA -> stringResource(R.string.isha_angle)
                else -> ""
            },
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.button,
            maxLines = 1,
        )
        Spacer(Modifier.height(paddingAroundPicker))
        Row(
            // Horizontal padding is 5.2%
            modifier = Modifier
                .fillMaxWidth(1 - 2 * 0.052f)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PickerGroup(
                *pickerGroupItems.toTypedArray(),
                modifier = Modifier.fillMaxWidth(),
                pickerGroupState = pickerGroupState,
                autoCenter = false,
            )
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
                        fajrAngleState.scrollToOption(angleToIndex(methodAngles.first))
                        ishaAngleState.scrollToOption(
                            if (ishaBlocked) 0 else angleToIndex(methodAngles.second)
                        )
                    }
                },
                colors = ButtonDefaults.secondaryButtonColors(),
                modifier = Modifier
                    .semantics {
                        focused =
                            pickerGroupState.selectedIndex == FocusableElementsPicker.RESET_BUTTON.index
                    }
                    .focusRequester(focusRequesterResetButton)
                    .onFocusChanged {
                        if (it.isFocused) onPickerSelected(
                            FocusableElementsPicker.RESET_BUTTON,
                            FocusableElementsPicker.CONFIRM_BUTTON
                        )
                    }
                    .focusable(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_replay_24),
                    contentDescription = stringResource(R.string.reset_to_defaults)
                )
            }
            Button(
                enabled = enabled,
                onClick = {
                    onConfirm(
                        indexToAngle(fajrAngleState.selectedOption),
                        if (ishaBlocked) methodAngles.second else indexToAngle(ishaAngleState.selectedOption)
                    )
                },
                colors = ButtonDefaults.primaryButtonColors(),
                modifier = Modifier
                    .semantics {
                        focused =
                            pickerGroupState.selectedIndex == FocusableElementsPicker.CONFIRM_BUTTON.index
                    }
                    .focusRequester(focusRequesterConfirmButton)
                    .focusable(),
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

@Composable
fun getPickerWidth(textStyle: TextStyle): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val digitWidth = remember(
        density.density,
        LocalConfiguration.current.screenWidthDp,
    ) {
        val mm = measurer.measure(
            "0123456789",
            style = textStyle,
            density = density,
        )

        (0..9).maxOf { mm.getBoundingBox(it).width }
    }
    return with(LocalDensity.current) { (digitWidth * 4.25f).toDp() + 6.dp }
}

fun angleToIndex(angle: Double): Int = ((angle - 9.0) * 2).toInt()
fun indexToAngle(index: Int): Double = 9 + (index.toDouble() / 2)

private enum class FocusableElementsPicker(val index: Int) {
    FAJR(0), ISHA(1), RESET_BUTTON(2), CONFIRM_BUTTON(3), NONE(-1), ;

    companion object {
        private val map = entries.associateBy { it.index }
        operator fun get(value: Int) = map[value]
    }
}

@WearPreviewDevices
@Composable
fun SettingsCustomAnglesScreenPreview() {
    SimplePrayerTheme {
        SettingsCustomAnglesScreen(
            state = SettingsState.Success(
                method = CalculationMethod.MOON_SIGHTING_COMMITTEE,
                madhab = Madhab.HANAFI,
                customAngles = 18.0 to 17.0,
                prayerAdjustments = UserPrayerAdjustments(
                    null, null, null, null, null, null
                )
            )
        ) { _, _ -> }
    }
}