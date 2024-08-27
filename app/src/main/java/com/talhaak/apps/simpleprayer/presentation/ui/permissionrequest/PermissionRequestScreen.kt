package com.talhaak.apps.simpleprayer.presentation.ui.permissionrequest

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionType: String,
    message: String,
    rationale: String,
    chipLabel: String,
    navigateOut: () -> Unit
) {
    var permissionAttempted by rememberSaveable { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(permissionType) { success ->
        if (!success) {
            permissionAttempted = true
        } else {
            navigateOut()
        }
    }

    PermissionRequestScreen(
        permissionState = locationPermissionState,
        permissionAttempted = permissionAttempted,
        message = message,
        rationale = rationale,
        chipLabel = chipLabel
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionState: PermissionState,
    permissionAttempted: Boolean,
    message: String,
    rationale: String,
    chipLabel: String
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    var show by rememberSaveable { mutableStateOf(false) }

    SettingsRedirectDialog(message = rationale, show = show, onDismiss = { show = false })

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title("Location")
            }
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(itemPadding())
                )
            }
            item {
                Chip(label = chipLabel, modifier = Modifier.padding(itemPadding()), onClick = {
                    if (permissionAttempted && !permissionState.status.shouldShowRationale) {
                        show = true
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@WearPreviewDevices
@Composable
fun PermissionRequestScreenPreview() {
    SimplePrayerTheme {
        PermissionRequestScreen(
            permissionState = object : PermissionState {
                override val permission = "test"
                override val status = PermissionStatus.Denied(false)
                override fun launchPermissionRequest() {}
            },
            permissionAttempted = true,
            message = "To calculate prayer times, location permission is needed.",
            rationale = "This is some flavour text to show as rationale",
            chipLabel = "Allow location"
        )
    }
}
