package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader

@Composable
fun SettingsScreen() {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text("Settings")
                }
            }
            item {
                Chip(
                    label = "Madhab",
                    secondaryLabel = "Hanafi",
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = {}
                )
            }
            item {
                Chip(
                    label = "Calculation Method",
                    colors = ChipDefaults.secondaryChipColors(),
                    secondaryLabel = "Custom",
                    onClick = {}
                )
            }
        }
    }
}