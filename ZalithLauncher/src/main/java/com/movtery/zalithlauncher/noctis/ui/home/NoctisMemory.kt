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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
 * Memory the selected instance will start with: its own override, or the global value.
 * Recomputed when the global setting changes, so the hero chip follows the dialog.
 */
@Composable
fun rememberEffectiveMemoryMb(version: Version?): Int {
    val context = LocalContext.current
    val stored = AllSettings.ramAllocation.state
    return remember(version, stored) {
        version?.getRamAllocation(context) ?: (stored ?: AllSettings.ramAllocation.min)
    }
}

/**
 * Dialog with the global memory slider (opened from the "3 GB" chip on the home).
 */
@Composable
fun NoctisMemoryDialog(
    version: Version?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.noctis_home_memory_title)) },
        text = {
            NoctisMemoryControl(
                modifier = Modifier.fillMaxWidth(),
                version = version
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.generic_close))
            }
        }
    )
}

@Composable
fun NoctisMemoryControl(
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    formatNoctisMemory(value.roundToInt()),
                    formatNoctisMemory(totalMb)
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
            instanceMb != null -> stringResource(R.string.noctis_home_memory_instance, formatNoctisMemory(instanceMb))
            isHigh -> stringResource(R.string.noctis_home_memory_high)
            else -> null
        }
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = if (instanceMb == null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 768 -> "768 MB", 3072 -> "3 GB", 3584 -> "3.5 GB" (decimal separator follows the phone language). */
fun formatNoctisMemory(mb: Int): String {
    if (mb < 1024) return "$mb MB"
    val gb = mb / 1024f
    return if (mb % 1024 == 0) "${mb / 1024} GB"
    else String.format(Locale.getDefault(), "%.1f GB", gb)
}
