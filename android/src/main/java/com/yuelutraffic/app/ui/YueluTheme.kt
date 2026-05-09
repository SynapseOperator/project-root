package com.yuelutraffic.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object YueluColors {
    val Page = Color(0xFFF7FAF7)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFE8F3EC)
    val Ink = Color(0xFF14231B)
    val InkMuted = Color(0xFF52645A)
    val CampusGreen = Color(0xFF1F7A4D)
    val CampusGreenSoft = Color(0xFFD8EFE2)
    val RoadOrange = Color(0xFFE58A2A)
    val AlertRed = Color(0xFFC43D32)
    val AdminBlue = Color(0xFF2468A2)
    val Divider = Color(0xFFD4DED8)
}

private val YueluScheme = lightColorScheme(
    primary = YueluColors.CampusGreen,
    onPrimary = Color.White,
    secondary = YueluColors.RoadOrange,
    onSecondary = Color.White,
    tertiary = YueluColors.AdminBlue,
    background = YueluColors.Page,
    onBackground = YueluColors.Ink,
    surface = YueluColors.Surface,
    onSurface = YueluColors.Ink,
    surfaceVariant = YueluColors.SurfaceMuted,
    onSurfaceVariant = YueluColors.InkMuted,
    error = YueluColors.AlertRed,
)

@Composable
fun YueluTrafficTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YueluScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
