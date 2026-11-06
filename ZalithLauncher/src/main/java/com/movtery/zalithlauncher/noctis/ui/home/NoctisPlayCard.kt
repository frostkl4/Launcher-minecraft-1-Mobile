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

package com.movtery.zalithlauncher.noctis.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.min
import com.movtery.zalithlauncher.ui.theme.NoctisPalette
import com.movtery.zalithlauncher.utils.platform.bytesToMB
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/** Memory slider snaps to this step, in MB. */
private const val MEMORY_STEP_MB = 128

/** Above this share of the allowed maximum the memory value turns into a warning. */
private const val MEMORY_WARNING_SHARE = 0.80f

/**
 * Top card of the home: the big Play button (with the selected instance)
 * and the memory slider, side by side.
 */
@Composable
fun NoctisPlayCard(
    version: Version?,
    isRefreshing: Boolean,
    onPlay: () -> Unit,
    onOpenInstanceSettings: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    NoctisPanel(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayButton(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                version = version,
                isRefreshing = isRefreshing,
                onPlay = onPlay,
                onLongPress = { version?.let(onOpenInstanceSettings) }
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(0.7f),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            MemoryControl(
                modifier = Modifier
                    .weight(0.44f)
                    .padding(end = 6.dp),
                version = version
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayButton(
    version: Version?,
    isRefreshing: Boolean,
    onPlay: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Small "press in" feedback: cheap, runs only while touching
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "playScale")
    val container by animateColorAsState(
        if (pressed) NoctisPalette.VioletPressed else NoctisPalette.Violet,
        label = "playColor"
    )

    val subtitle = when {
        isRefreshing && version == null -> stringResource(R.string.noctis_home_loading)
        version == null -> stringResource(R.string.noctis_home_no_instance)
        else -> version.getVersionName()
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(container, container.copy(red = container.red * 0.85f, blue = container.blue * 0.92f))
                )
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.noctis_home_instance_options),
                onLongClick = onLongPress,
                onClick = onPlay
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = null,
                tint = Color.White
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.noctis_home_play),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MemoryControl(
    version: Version?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val minMb = AllSettings.ramAllocation.min
    val maxMb = remember { max(getMaxMemoryForSettings(context), minMb + MEMORY_STEP_MB) }
    val totalMb = remember { getTotalMemory(context).bytesToMB(decimals = 0).roundToInt() }

    val stored = AllSettings.ramAllocation.state ?: minMb
    var value by remember(stored, maxMb) {
        mutableFloatStateOf(stored.coerceIn(minMb, maxMb).toFloat())
    }

    // An instance can override the global memory in its own settings
    val instanceMb = remember(version) {
        version?.getVersionConfig()?.ramAllocation?.takeIf { it >= 256 }
    }
    val isHigh = value >= maxMb * MEMORY_WARNING_SHARE

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.noctis_home_memory),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = stringResource(
                    R.string.noctis_home_memory_value,
                    formatMemory(value.roundToInt()),
                    formatMemory(totalMb)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (isHigh) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { raw ->
                val snapped = (raw / MEMORY_STEP_MB).roundToInt() * MEMORY_STEP_MB
                value = snapped.coerceIn(minMb, maxMb).toFloat()
            },
            onValueChangeFinished = {
                AllSettings.ramAllocation.save(value.roundToInt())
            },
            valueRange = minMb.toFloat()..maxMb.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = if (isHigh) MaterialTheme.colorScheme.tertiary else NoctisPalette.Lavender,
                activeTrackColor = if (isHigh) MaterialTheme.colorScheme.tertiary else NoctisPalette.Violet,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )

        val hint = when {
            instanceMb != null -> stringResource(R.string.noctis_home_memory_instance, formatMemory(instanceMb))
            isHigh -> stringResource(R.string.noctis_home_memory_high)
            else -> null
        }
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = if (instanceMb == null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 768 -> "768 MB", 3072 -> "3.0 GB" (decimal separator follows the phone language). */
private fun formatMemory(mb: Int): String {
    return if (mb < 1024) "$mb MB"
    else String.format(Locale.getDefault(), "%.1f GB", mb / 1024f)
}
