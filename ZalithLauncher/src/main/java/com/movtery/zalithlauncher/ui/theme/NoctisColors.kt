/*
 * Noctis Launcher — unofficial modified version of Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 * Modifications Copyright (C) 2026 frostkl4
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

/**
 * Noctis palette: near-black violet night, one strong violet for the main
 * action (Play), lavender for text accents and a soft moon pink as tertiary.
 */
object NoctisPalette {
    /** Strong violet used by the Play button. White text on it is about 4.8:1. */
    val Violet = Color(0xFF7C4DFF)
    val VioletPressed = Color(0xFF6A3FE0)
    val Lavender = Color(0xFFCDB8FF)
    val MoonPink = Color(0xFFF3B6DC)
    val MoonGlow = Color(0xFFE9DDFF)

    /** Sky gradient, top to bottom. */
    val SkyTop = Color(0xFF2A1752)
    val SkyMiddle = Color(0xFF170E30)
    val SkyBottom = Color(0xFF0B0716)

    /** Mountain layers, far to near. */
    val MountainFar = Color(0xFF3A2766)
    val MountainMid = Color(0xFF241848)
    val MountainNear = Color(0xFF120B24)
    val SnowTint = Color(0xFF8E6FD8)
}

val noctisDarkScheme: ColorScheme = darkColorScheme(
    primary = NoctisPalette.Violet,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3B2470),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFCDB8FF),
    onSecondary = Color(0xFF2A1752),
    secondaryContainer = Color(0xFF2E2450),
    onSecondaryContainer = Color(0xFFE6DEFF),
    tertiary = NoctisPalette.MoonPink,
    onTertiary = Color(0xFF4A1A3A),
    tertiaryContainer = Color(0xFF5E2A4E),
    onTertiaryContainer = Color(0xFFFFD8EE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B0814),
    onBackground = Color(0xFFE8E2F4),
    surface = Color(0xFF0F0B1A),
    onSurface = Color(0xFFE8E2F4),
    surfaceVariant = Color(0xFF2A2438),
    onSurfaceVariant = Color(0xFFCAC3DA),
    outline = Color(0xFF938CA3),
    outlineVariant = Color(0xFF3D3650),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8E2F4),
    inverseOnSurface = Color(0xFF2F2A3A),
    inversePrimary = Color(0xFF6A3FE0),
    surfaceDim = Color(0xFF0F0B1A),
    surfaceBright = Color(0xFF2B2440),
    surfaceContainerLowest = Color(0xFF07050D),
    surfaceContainerLow = Color(0xFF15111F),
    surfaceContainer = Color(0xFF1A1526),
    surfaceContainerHigh = Color(0xFF241E31),
    surfaceContainerHighest = Color(0xFF2F283D),
)

val noctisLightScheme: ColorScheme = dynamicColorScheme(
    primary = NoctisPalette.VioletPressed,
    isDark = false,
    style = PaletteStyle.TonalSpot,
)
