package com.yagubogu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = Primary600,
        onPrimary = Gray900,
        primaryContainer = Primary800,
        onPrimaryContainer = Primary100,
        secondary = Gray400,
        onSecondary = Gray900,
        secondaryContainer = Gray700,
        onSecondaryContainer = Gray100,
        tertiary = Primary300,
        onTertiary = Gray900,
        background = Gray900,
        onBackground = Gray100,
        surface = Gray800,
        onSurface = Gray100,
        surfaceVariant = Gray700,
        onSurfaceVariant = Gray300,
        outline = Gray600,
        outlineVariant = Gray700,
        error = Red,
        onError = White,
        inverseSurface = Gray100,
        inverseOnSurface = Gray800,
        inversePrimary = Primary600,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Primary500,
        onPrimary = White,
        primaryContainer = Primary100,
        onPrimaryContainer = Primary900,
        secondary = Gray600,
        onSecondary = White,
        secondaryContainer = Gray100,
        onSecondaryContainer = Gray900,
        tertiary = Primary700,
        onTertiary = White,
        background = White,
        onBackground = Gray900,
        surface = Gray050,
        onSurface = Gray900,
        surfaceVariant = Gray100,
        onSurfaceVariant = Gray700,
        outline = Gray400,
        outlineVariant = Gray300,
        error = Red,
        onError = White,
        inverseSurface = Gray800,
        inverseOnSurface = Gray100,
        inversePrimary = Primary400,
    )

@Composable
fun YaguBoguTheme(
    // TODO 다크모드 지원시 변경
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
