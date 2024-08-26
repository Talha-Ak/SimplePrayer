package com.talhaak.apps.simpleprayer.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.horologist.compose.material.CompactChip
import com.google.android.horologist.compose.material.util.DECORATIVE_ELEMENT_CONTENT_DESCRIPTION
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme

@Composable
fun SettingsRedirectDialog(message: String, show: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        showDialog = show, onDismissRequest = onDismiss
    ) {
        SettingsRedirectAlert(message) {
            onDismiss()
            openAppSettings(context)
        }
    }
}

@Composable
fun SettingsRedirectAlert(message: String, onClick: () -> Unit) {
    Alert(icon = {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = DECORATIVE_ELEMENT_CONTENT_DESCRIPTION
        )
    }, title = {
        Text(
            text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.title3
        )
    }) {
        item {
            CompactChip(
                label = "Settings",
                onClick = onClick,
                icon = Icons.Default.Settings.asPaintable(),
            )
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

@WearPreviewDevices
@Composable
fun SettingsRedirectAlertPreview() {
    SimplePrayerTheme {
        SettingsRedirectAlert("You need to grant location permission to use this app") {}
    }
}