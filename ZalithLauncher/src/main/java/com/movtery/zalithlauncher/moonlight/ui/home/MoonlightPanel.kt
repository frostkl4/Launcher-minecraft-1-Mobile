/*
 * Moonlight Launcher — unofficial modified version of Zalith Launcher 2
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

package com.movtery.zalithlauncher.moonlight.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Spacing and shape tokens for the Moonlight home (8dp grid). */
object MoonlightTokens {
    val PanelShape = RoundedCornerShape(24.dp)
    val CardShape = RoundedCornerShape(20.dp)
    val InnerShape = RoundedCornerShape(14.dp)
    val PillShape = RoundedCornerShape(50)
}

/**
 * Translucent dark panel that lets the night sky show through a little.
 * Plain alpha instead of a real-time blur: blur costs GPU time on every frame
 * and only exists on Android 12+.
 */
@Composable
fun MoonlightPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MoonlightTokens.PanelShape,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.80f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        content = content
    )
}
