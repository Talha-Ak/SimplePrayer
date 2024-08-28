package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SelectableChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.placeholderShimmer
import androidx.wear.compose.material.rememberPlaceholderState
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SettingsChip(
    label: String,
    secondaryLabel: String? = null,
    description: String? = null,
    onClick: () -> Unit
) {
    val loadingState = rememberPlaceholderState { !secondaryLabel.isNullOrEmpty() }
    Column(modifier = if (description != null) Modifier.padding(bottom = 12.dp) else Modifier) {
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
        description?.let {
            Text(
                text = description,
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.secondaryVariant,
                modifier = Modifier
                    .listTextPadding()
                    .padding(top = 8.dp)
            )
        }
    }

    if (!loadingState.isShowContent) {
        LaunchedEffect(loadingState) {
            loadingState.startPlaceholderAnimation()
        }
    }
}

@Composable
fun SettingsSelectableChip(
    selected: Boolean,
    onSelected: () -> Unit,
    label: String,
    secondaryLabel: String? = null,
) {
    SelectableChip(
        label = {
            Text(label)
        },
        secondaryLabel = {
            secondaryLabel?.let {
                Text(secondaryLabel)
            }
        },
        selected = selected,
        onClick = { checked -> if (checked) onSelected() },
        modifier = Modifier.fillMaxWidth()
    )
}