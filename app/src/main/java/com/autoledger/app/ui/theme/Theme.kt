package com.autoledger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = YS.brand,
    onPrimary = YS.white,
    primaryContainer = YS.brandTint,
    onPrimaryContainer = YS.brandDeep,
    secondary = YS.brandDeep,
    onSecondary = YS.white,
    background = YS.bg,
    onBackground = YS.ink,
    surface = YS.surface,
    onSurface = YS.ink,
    surfaceVariant = YS.surface2,
    onSurfaceVariant = YS.inkSoft,
    outline = YS.line,
    error = YS.expense,
    onError = YS.white
)

/** 设计稿为浅色暖调，强制浅色主题以保证「一模一样」的视觉 */
@Composable
fun AutoLedgerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
