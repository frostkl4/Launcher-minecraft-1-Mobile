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

package com.movtery.zalithlauncher.noctis.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R

/** Where the rail is pointing right now. */
enum class NoctisDestination { Home, Download, Multiplayer, Settings, Other }

private val RailItemSize = 44.dp
private val RailItemShape = RoundedCornerShape(14.dp)

/**
 * Vertical navigation rail that replaces the original top bar.
 * Phones in landscape have little height, so all navigation lives on the left edge.
 */
@Composable
fun NoctisRail(
    current: NoctisDestination,
    isHome: Boolean,
    hasTasks: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onDownload: () -> Unit,
    onMultiplayer: () -> Unit,
    onFiles: () -> Unit,
    onSettings: () -> Unit,
    onTasks: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f))
    ) {
    // Scrolls only on very short screens; otherwise settings stays pinned to the bottom
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .heightIn(min = maxHeight)
            .width(width)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Logo on the home screen, back arrow everywhere else
        AnimatedContent(
            targetState = isHome,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.8f)) },
            label = "railTop"
        ) { home ->
            if (home) {
                Image(
                    modifier = Modifier
                        .size(RailItemSize)
                        .clip(RailItemShape),
                    painter = painterResource(R.drawable.img_noctis_logo),
                    contentDescription = null
                )
            } else {
                RailButton(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    label = stringResource(R.string.noctis_rail_back),
                    selected = false,
                    onClick = onBack
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        RailButton(
            painter = painterResource(R.drawable.ic_home_filled),
            label = stringResource(R.string.noctis_rail_home),
            selected = current == NoctisDestination.Home,
            onClick = onHome
        )
        RailButton(
            painter = painterResource(R.drawable.ic_download_2_filled),
            label = stringResource(R.string.generic_download),
            selected = current == NoctisDestination.Download,
            onClick = onDownload
        )
        RailButton(
            painter = painterResource(R.drawable.ic_group_filled),
            label = stringResource(R.string.terracotta),
            selected = current == NoctisDestination.Multiplayer,
            onClick = onMultiplayer
        )
        RailButton(
            painter = painterResource(R.drawable.ic_folder_filled),
            label = stringResource(R.string.noctis_rail_files),
            selected = false,
            onClick = onFiles
        )

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = hasTasks) {
            Box(
                modifier = Modifier
                    .size(RailItemSize)
                    .clip(RailItemShape)
                    .clickable(role = Role.Button, onClick = onTasks),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp
                )
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(R.drawable.ic_assignment_filled),
                    contentDescription = stringResource(R.string.noctis_rail_tasks)
                )
            }
        }

        RailButton(
            painter = painterResource(R.drawable.ic_settings_filled),
            label = stringResource(R.string.generic_setting),
            selected = current == NoctisDestination.Settings,
            onClick = onSettings
        )
    }
    }
}

@Composable
private fun RailButton(
    painter: Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f),
        label = "railContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "railContent"
    )
    Box(
        modifier = Modifier
            .size(RailItemSize)
            .clip(RailItemShape)
            .background(container)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics {
                this.selected = selected
                contentDescription = label
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painter,
            tint = content,
            contentDescription = null
        )
    }
}
