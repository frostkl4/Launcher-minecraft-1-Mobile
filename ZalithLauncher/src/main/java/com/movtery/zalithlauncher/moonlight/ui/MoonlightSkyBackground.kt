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

package com.movtery.zalithlauncher.moonlight.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.theme.MoonlightPalette
import java.util.Random
import kotlin.math.min

/**
 * Night sky with a full moon, pixel stars and three mountain layers.
 *
 * Drawn with vector shapes instead of a wallpaper bitmap: it costs no image
 * memory, stays sharp on any screen and is only rebuilt when the size changes.
 * It is completely static, so it never forces extra frames.
 */
@Composable
fun MoonlightSkyBackground(modifier: Modifier = Modifier) {
    Spacer(
        // Own layer: the sky is recorded once and reused, instead of being re-recorded
        // every time something above it redraws
        modifier = modifier
            .graphicsLayer()
            .drawWithCache {
            val w = size.width
            val h = size.height
            val pixel = 2.dp.toPx()

            val sky = Brush.verticalGradient(
                0f to MoonlightPalette.SkyTop,
                0.55f to MoonlightPalette.SkyMiddle,
                1f to MoonlightPalette.SkyBottom
            )

            // Moon in the upper right, like the reference art
            val moonRadius = min(w, h) * 0.105f
            val moonCenter = Offset(w * 0.80f, h * 0.24f)
            val glow = Brush.radialGradient(
                colors = listOf(
                    MoonlightPalette.MoonGlow.copy(alpha = 0.30f),
                    MoonlightPalette.MoonPink.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = moonCenter,
                radius = moonRadius * 3.4f
            )
            val moonFill = Brush.radialGradient(
                colors = listOf(Color(0xFFF6F0FF), MoonlightPalette.MoonGlow, Color(0xFFD9C3F5)),
                center = moonCenter - Offset(moonRadius * 0.3f, moonRadius * 0.3f),
                radius = moonRadius * 1.4f
            )
            val craterColor = Color(0xFFB9A2E6).copy(alpha = 0.45f)
            val craters = listOf(
                Triple(-0.30f, -0.15f, 0.22f),
                Triple(0.25f, 0.20f, 0.16f),
                Triple(0.05f, -0.45f, 0.10f),
                Triple(-0.10f, 0.40f, 0.12f),
                Triple(0.45f, -0.20f, 0.08f)
            )

            // Pixel stars: same seed every time, so the sky never "jumps"
            val random = Random(0x4E6F6374L)
            val stars = List(110) {
                val x = random.nextFloat() * w
                val y = random.nextFloat() * h * 0.66f
                val big = random.nextFloat() < 0.08f
                val alpha = 0.35f + random.nextFloat() * 0.65f
                Star(Offset(x, y), big, alpha)
            }.filter { (it.position - moonCenter).getDistance() > moonRadius * 1.6f }

            val far = mountainPath(w, h, Random(11L), baseline = 0.70f, low = 0.52f, high = 0.36f, peaks = 7)
            val mid = mountainPath(w, h, Random(23L), baseline = 0.80f, low = 0.66f, high = 0.48f, peaks = 6)
            val near = mountainPath(w, h, Random(37L), baseline = 0.92f, low = 0.84f, high = 0.70f, peaks = 9)
            val midFill = Brush.verticalGradient(
                0f to MoonlightPalette.SnowTint.copy(alpha = 0.55f),
                0.35f to MoonlightPalette.MountainMid,
                1f to MoonlightPalette.MountainMid,
                startY = h * 0.48f,
                endY = h
            )

            onDrawBehind {
                drawRect(sky)
                drawCircle(glow, radius = moonRadius * 3.4f, center = moonCenter)

                stars.forEach { star ->
                    val color = Color.White.copy(alpha = star.alpha)
                    drawRect(color, star.position, Size(pixel, pixel))
                    if (star.big) {
                        // 4-point pixel sparkle
                        drawRect(color, star.position + Offset(-pixel, 0f), Size(pixel, pixel))
                        drawRect(color, star.position + Offset(pixel, 0f), Size(pixel, pixel))
                        drawRect(color, star.position + Offset(0f, -pixel), Size(pixel, pixel))
                        drawRect(color, star.position + Offset(0f, pixel), Size(pixel, pixel))
                    }
                }

                drawCircle(moonFill, radius = moonRadius, center = moonCenter)
                craters.forEach { (dx, dy, r) ->
                    drawCircle(
                        color = craterColor,
                        radius = moonRadius * r,
                        center = moonCenter + Offset(moonRadius * dx, moonRadius * dy)
                    )
                }

                drawPath(far, MoonlightPalette.MountainFar)
                drawPath(mid, midFill)
                drawPath(near, MoonlightPalette.MountainNear)
            }
        }
    )
}

private class Star(val position: Offset, val big: Boolean, val alpha: Float)

/**
 * Jagged mountain ridge. Heights are fractions of the canvas height.
 */
private fun mountainPath(
    w: Float,
    h: Float,
    random: Random,
    baseline: Float,
    low: Float,
    high: Float,
    peaks: Int
): Path = Path().apply {
    moveTo(0f, h * baseline)
    val step = w / peaks
    var x = 0f
    for (i in 0 until peaks) {
        val valleyY = h * (low + random.nextFloat() * (baseline - low) * 0.5f)
        val peakY = h * (high + random.nextFloat() * (low - high) * 0.6f)
        val peakX = x + step * (0.35f + random.nextFloat() * 0.3f)
        // a small shoulder before the peak makes the ridge look less regular
        lineTo(peakX - step * 0.12f, (valleyY + peakY) / 2f + h * 0.02f)
        lineTo(peakX, peakY)
        lineTo(x + step, valleyY)
        x += step
    }
    lineTo(w, h)
    lineTo(0f, h)
    close()
}
