package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GlassPrimaryDark,
    onPrimary = GlassOnPrimaryDark,
    primaryContainer = GlassPrimaryContainerDark,
    onPrimaryContainer = GlassOnPrimaryContainerDark,
    background = GlassBackgroundDark,
    onBackground = GlassOnBackgroundDark,
    surface = GlassSurfaceDark,
    onSurface = GlassOnSurfaceDark,
    surfaceVariant = GlassSurfaceVariantDark,
    onSurfaceVariant = GlassOnSurfaceVariantDark,
    outline = GlassOutline
)

private val LightColorScheme = lightColorScheme(
    primary = GlassPrimary,
    onPrimary = GlassOnPrimary,
    primaryContainer = GlassPrimaryContainer,
    onPrimaryContainer = GlassOnPrimaryContainer,
    secondary = GlassSecondary,
    onSecondary = GlassOnSecondary,
    secondaryContainer = GlassSecondaryContainer,
    onSecondaryContainer = GlassOnSecondaryContainer,
    tertiary = GlassTertiary,
    onTertiary = GlassOnTertiary,
    background = GlassBackground,
    onBackground = GlassOnBackground,
    surface = GlassSurface,
    onSurface = GlassOnSurface,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = GlassOnSurfaceVariant,
    outline = GlassOutline,
    outlineVariant = GlassOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Standard option, disabled by default to guarantee the bespoke Frosted Glass palette shows
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
