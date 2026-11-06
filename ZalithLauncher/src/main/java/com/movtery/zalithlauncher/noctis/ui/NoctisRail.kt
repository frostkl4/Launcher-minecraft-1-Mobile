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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R

/** Where the sidebar is pointing right now. */
enum class NoctisDestination { Home, Library, Explore, Skins, Multiplayer, Settings, Other }

private class RailEntry(
    val destination: NoctisDestination,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
)

/** Main destinations, top to bottom. Settings is pinned to the bottom separately. */
private val RailEntries = listOf(
    RailEntry(NoctisDestination.Home, R.drawable.ic_noctis_home, R.string.noctis_rail_home),
    RailEntry(NoctisDestination.Library, R.drawable.ic_noctis_library, R.string.noctis_rail_library),
    RailEntry(NoctisDestination.Explore, R.drawable.ic_noctis_explore, R.string.noctis_rail_explore),
    RailEntry(NoctisDestination.Skins, R.drawable.ic_noctis_skins, R.string.noctis_rail_skins),
    RailEntry(NoctisDestination.Multiplayer, R.drawable.ic_noctis_multiplayer, R.string.noctis_rail_multiplayer),
)

private val RailItemSize = 44.dp
private val RailItemShape = RoundedCornerShape(14.dp)

/**
 * Left sidebar of the launcher (SKLauncher / Modrinth App style).
 *
 * Phones in landscape have little height, so every destination lives on the
 * left edge and the content gets the whole height. On very short screens the
 * column scrolls instead of clipping; otherwise Settings stays pinned to the bottom.
 *
 * @param translucent true when the user has a custom wallpaper behind the launcher
 */
@Composable
fun NoctisRail(
    current: NoctisDestination,
    isHome: Boolean,
    hasTasks: Boolean,
    translucent: Boolean,
    onBack: () -> Unit,
    onNavigate: (NoctisDestination) -> Unit,
    onTasks: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
) {
    val railColor = MaterialTheme.colorScheme.surfaceContainerLowest.let {
        if (translucent) it.copy(alpha = 0.72f) else it
    }
    val edgeColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(railColor)
            .drawBehind {
                // 1px divider on the content side
                val x = size.width - 0.5f
                drawLine(edgeColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .width(width)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Logo on the home screen, back arrow everywhere else
            AnimatedContent(
                targetState = isHome,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.8f))
                },
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
                        icon = R.drawable.ic_arrow_back,
                        label = stringResource(R.string.noctis_rail_back),
                        selected = false,
                        role = Role.Button,
                        onClick = onBack
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            RailEntries.forEach { entry ->
                RailButton(
                    icon = entry.icon,
                    label = stringResource(entry.label),
                    selected = current == entry.destination,
                    onClick = { onNavigate(entry.destination) }
                )
            }

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
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(R.drawable.ic_noctis_tasks),
                        contentDescription = stringResource(R.string.noctis_rail_tasks),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            RailButton(
                icon = R.drawable.ic_noctis_settings,
                label = stringResource(R.string.generic_setting),
                selected = current == NoctisDestination.Settings,
                onClick = { onNavigate(NoctisDestination.Settings) }
            )
        }
    }
}

@Composable
private fun RailButton(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    role: Role = Role.Tab,
) {
    val accent = MaterialTheme.colorScheme.secondary
    val container by animateColorAsState(
        targetValue = accent.copy(alpha = if (selected) 0.16f else 0f),
        label = "railContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "railContent"
    )
    Box(
        modifier = Modifier
            .size(RailItemSize)
            .clip(RailItemShape)
            .background(container)
            .clickable(role = role, onClick = onClick)
            .semantics {
                if (role == Role.Tab) this.selected = selected
                contentDescription = label
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            tint = content,
            contentDescription = null
        )
    }
}
