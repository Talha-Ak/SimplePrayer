package com.talhaak.apps.simpleprayer.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Title
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.talhaak.apps.simpleprayer.BuildConfig
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import kotlinx.coroutines.launch

@Composable
fun AboutScreen() {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                Title(textId = R.string.about)
            }
            item {
                Text(text = stringResource(R.string.app_name))
            }
            item {
                Text(text = stringResource(R.string.version, BuildConfig.VERSION_NAME))
            }
            item {
                Chip(
                    label = "Privacy Policy",
                    onClick = {
                        coroutineScope.launch {
                            launchPrivacyPolicy(context)
                        }
                    },
                    icon = ImageVector.vectorResource(id = R.drawable.open_in_phone).asPaintable(),
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

const val PRIVACY_POLICY_URL = "https://apps.talhaak.com/privacy=policy/"

private suspend fun launchPrivacyPolicy(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
        .addCategory(Intent.CATEGORY_BROWSABLE)

    val remoteActivityHelper = RemoteActivityHelper(context)
    remoteActivityHelper.availabilityStatus.collect { status ->
        when (status) {
            RemoteActivityHelper.STATUS_AVAILABLE, RemoteActivityHelper.STATUS_UNKNOWN ->
                remoteActivityHelper.startRemoteActivity(intent)

            else -> Unit
        }
    }
}

@WearPreviewDevices
@Composable
fun AboutScreenPreview() {
    SimplePrayerTheme {
        AboutScreen()
    }
}
