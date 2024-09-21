package com.talhaak.apps.simpleprayer.presentation.ui.permissionrequest

import android.Manifest
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Title
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionType: String,
    navigateOut: () -> Unit
) {
    val icon = getIcon(permissionType)
    val (title, message, rationale, chipLabel) = getContents(permissionType)

    var permissionAttempted by remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(permissionType) { success ->
        if (!success) permissionAttempted = true
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) navigateOut()
    }

    // Runtime permissions are a joke
    // Can only distinguish between first-run and "do not ask" by attempting permission
    val showDialog = permissionAttempted && !locationPermissionState.status.shouldShowRationale

    SettingsRedirectDialog(
        icon = ImageVector.vectorResource(icon),
        message = stringResource(rationale),
        show = showDialog,
        onDismiss = { permissionAttempted = false }
    )

    PermissionRequestScreen(
        titleId = title,
        messageId = message,
        chipLabelId = chipLabel,
        launchRequest = { locationPermissionState.launchPermissionRequest() }
    )
}

@Composable
fun PermissionRequestScreen(
    titleId: Int,
    messageId: Int,
    chipLabelId: Int,
    launchRequest: () -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(textId = titleId)
            }
            item {
                Text(
                    text = stringResource(messageId),
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.listTextPadding()
                )
            }
            item {
                Chip(
                    labelId = chipLabelId,
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = launchRequest
                )
            }
        }
    }
}

private fun getContents(permissionType: String) = when (permissionType) {
    Manifest.permission.ACCESS_COARSE_LOCATION -> listOf(
        R.string.location,
        R.string.location_request_message,
        R.string.location_request_rationale,
        R.string.location_request_allow_button
    )

    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> listOf(
        R.string.background_location,
        R.string.background_location_request_message,
        R.string.background_location_request_rationale,
        R.string.accept
    )

    else -> throw IllegalArgumentException("Unknown permission type")
}

private fun getIcon(permissionType: String): Int = when (permissionType) {
    Manifest.permission.ACCESS_COARSE_LOCATION -> R.drawable.baseline_location_on_24
    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> R.drawable.baseline_location_on_24
    else -> throw IllegalArgumentException("Unknown permission type")
}


@WearPreviewDevices
@Composable
fun PermissionRequestScreenPreview() {
    SimplePrayerTheme {
        PermissionRequestScreen(
            R.string.location,
            R.string.location_request_message,
            R.string.location_request_allow_button,
            {}
        )
    }
}
