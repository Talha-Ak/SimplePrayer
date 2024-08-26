package com.talhaak.apps.simpleprayer.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

internal val wearColorPalette: Colors = Colors(
    primary = primaryDark,
    primaryVariant = primaryContainerDarkMediumContrast,
    onPrimary = onPrimaryDark,
    secondary = secondaryDark,
    secondaryVariant = secondaryContainerDarkMediumContrast,
    onSecondary = onSecondaryDark,
    surface = surfaceDark,
    onSurface = onSurfaceVariantDark,
    onSurfaceVariant = onSurfaceDark,
    error = errorDark,
    onError = onErrorDark,
//    background = backgroundDark,
)

@Composable
fun SimplePrayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        content = content
    )
}