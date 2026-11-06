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

package com.movtery.zalithlauncher.moonlight.ui.project

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.MoonlightPalette

private val InstalledMint = Color(0xFF7EE0B0)

/** Text tabs with a lavender underline (Description · Gallery · Versions · Dependencies). */
@Composable
fun MoonlightTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Row(
        modifier = modifier.drawBehind {
            drawLine(lineColor, Offset(0f, size.height - 0.5f), Offset(size.width, size.height - 0.5f), 1f)
        },
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selected
            val underline by animateFloatAsState(if (isSelected) 1f else 0f, label = "tabLine")
            val accent = MaterialTheme.colorScheme.secondary
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                    .drawBehind {
                        if (underline > 0f) {
                            val w = size.width * underline
                            drawLine(
                                color = accent,
                                start = Offset((size.width - w) / 2f, size.height - 1.5f),
                                end = Offset((size.width + w) / 2f, size.height - 1.5f),
                                strokeWidth = 3f
                            )
                        }
                    }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/** Small uppercase heading of the install panel ("INSTALL"). */
@Composable
fun MoonlightPanelHeading(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MoonlightPalette.Lavender
    )
}

/** One option of a [MoonlightSelectField]. */
class MoonlightOption<T>(
    val value: T,
    val label: String,
)

/**
 * Labelled field that opens a dropdown ("Install in", "Version").
 * Shows [placeholder] when nothing is selected or there is nothing to pick.
 */
@Composable
fun <T> MoonlightSelectField(
    label: String,
    options: List<MoonlightOption<T>>,
    selected: MoonlightOption<T>?,
    onSelect: (MoonlightOption<T>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "—",
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                onClick = { if (options.isNotEmpty()) expanded = true },
                enabled = enabled && options.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = selected?.label ?: placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DropdownMenu(
                modifier = Modifier.heightIn(max = 300.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(14.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selected?.value
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null
                                )
                            }
                        } else null,
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/** What the big install button shows. */
sealed interface InstallButtonState {
    data object Loading : InstallButtonState
    data object Ready : InstallButtonState
    data object Unavailable : InstallButtonState
    /** @param progress 0..1, or below 0 while copying into the instance */
    data class Downloading(val progress: Float) : InstallButtonState
    data object Installed : InstallButtonState
}

/**
 * Big install button of the project page. While downloading it turns into a
 * progress bar ("Downloading… 42%"), like the concept.
 */
@Composable
fun MoonlightInstallButton(
    state: InstallButtonState,
    readyLabel: String,
    loadingLabel: String,
    installedLabel: String,
    downloadingLabel: (Int) -> String,
    installingLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    val clickable = state is InstallButtonState.Ready
    val container by animateColorAsState(
        when (state) {
            InstallButtonState.Installed -> InstalledMint.copy(alpha = 0.16f)
            is InstallButtonState.Downloading -> MoonlightPalette.Violet.copy(alpha = 0.26f)
            InstallButtonState.Ready -> MoonlightPalette.Violet
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "installButton"
    )
    val progress by animateFloatAsState(
        targetValue = (state as? InstallButtonState.Downloading)?.progress?.coerceIn(0f, 1f) ?: 0f,
        label = "installProgress"
    )

    Surface(
        modifier = modifier.height(50.dp),
        onClick = onClick,
        enabled = clickable,
        shape = shape,
        color = container,
        contentColor = when (state) {
            InstallButtonState.Installed -> InstalledMint
            InstallButtonState.Ready, is InstallButtonState.Downloading -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state is InstallButtonState.Downloading) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (state.progress < 0f) 1f else progress)
                        .clip(shape)
                        .background(MoonlightPalette.Violet)
                )
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(
                        if (state == InstallButtonState.Installed) R.drawable.ic_check else R.drawable.ic_download_2_filled
                    ),
                    contentDescription = null
                )
                Text(
                    text = when (state) {
                        InstallButtonState.Loading -> loadingLabel
                        InstallButtonState.Ready, InstallButtonState.Unavailable -> readyLabel
                        InstallButtonState.Installed -> installedLabel
                        is InstallButtonState.Downloading -> if (state.progress < 0f) {
                            installingLabel
                        } else {
                            downloadingLabel((state.progress * 100).toInt().coerceIn(0, 100))
                        }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/** Pink note under the version ("Includes Sodium (dependency)"). */
@Composable
fun MoonlightDependencyNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MoonlightPalette.MoonPink.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = MoonlightPalette.MoonPink
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MoonlightPalette.MoonPink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
