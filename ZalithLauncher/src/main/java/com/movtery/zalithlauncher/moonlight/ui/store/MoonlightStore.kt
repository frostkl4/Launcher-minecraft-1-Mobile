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

package com.movtery.zalithlauncher.moonlight.ui.store

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightTag
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.AddonIcon
import com.movtery.zalithlauncher.ui.theme.MoonlightPalette
import com.movtery.zalithlauncher.utils.formatNumberByLocale

/** Mint used for "Installed". */
private val InstalledMint = Color(0xFF7EE0B0)

private val CardShape = RoundedCornerShape(18.dp)
private val PillShape = RoundedCornerShape(50)

/**
 * Rounded search field at the top of the store. Search runs on the keyboard's
 * search key (the results are paged API calls, not a live filter).
 */
@Composable
fun MoonlightSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        modifier = modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f), PillShape)
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = placeholder },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MoonlightPalette.Lavender),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onSearch()
        }),
        decorationBox = { inner ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    inner()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                onValueChange("")
                                focusManager.clearFocus()
                                onSearch()
                            }
                            .padding(7.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.moonlight_home_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

/** Modrinth | CurseForge switch. */
@Composable
fun MoonlightPlatformToggle(
    selected: Platform,
    onSelect: (Platform) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.moonlight_store_platform)
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), PillShape)
            .padding(3.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(Platform.MODRINTH to "Modrinth", Platform.CURSEFORGE to "CurseForge").forEach { (platform, name) ->
            val isSelected = platform == selected
            val container by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
                label = "platformPill"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(PillShape)
                    .background(container)
                    .clickable(role = Role.Tab) { if (!isSelected) onSelect(platform) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/** "Filters (n)" button that opens the side panel. */
@Composable
fun MoonlightFilterButton(
    activeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = PillShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_filter_alt_outlined),
                contentDescription = null
            )
            Text(
                text = if (activeCount > 0) {
                    stringResource(R.string.moonlight_store_filters_count, activeCount)
                } else {
                    stringResource(R.string.moonlight_store_filters)
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** Quick filter pill ("Fabric", "1.20.1", "Optimization"…). */
@Composable
fun MoonlightPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(
        if (selected) MoonlightPalette.Violet.copy(alpha = 0.30f) else Color.Transparent,
        label = "pillContainer"
    )
    val border by animateColorAsState(
        if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        label = "pillBorder"
    )
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(PillShape)
            .background(container)
            .border(1.dp, border, PillShape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selected) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MoonlightPalette.MoonGlow
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MoonlightPalette.MoonGlow else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/** Thin divider between groups of pills. */
@Composable
fun MoonlightPillDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 2.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * Project card of the store grid: official icon (loaded in the background and
 * cached by the app's image loader), title, author, downloads, tags and Install.
 */
@Composable
fun MoonlightProjectCard(
    title: String,
    description: String,
    iconUrl: String?,
    authors: List<String>?,
    downloads: Long,
    follows: Long?,
    tags: List<String>,
    isInstalled: Boolean,
    onOpen: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val downloadsText = remember(downloads) { formatNumberByLocale(context, downloads) }
    val followsText = remember(follows) { follows?.let { formatNumberByLocale(context, it) } }

    Surface(
        modifier = modifier,
        onClick = onOpen,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddonIcon(
                    modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                    size = 56.dp,
                    iconUrl = iconUrl
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!authors.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.moonlight_store_by, authors.joinToString(", ")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Stat(icon = R.drawable.ic_download_2_filled, text = downloadsText)
                        if (followsText != null) Stat(icon = R.drawable.ic_star_filled, text = followsText)
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.take(2).forEach { MoonlightTag(text = it) }
                }
                Spacer(Modifier.width(8.dp))
                InstallPill(
                    title = title,
                    isInstalled = isInstalled,
                    onClick = onInstall
                )
            }
        }
    }
}

@Composable
private fun Stat(icon: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(12.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun InstallPill(
    title: String,
    isInstalled: Boolean,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.moonlight_store_install_project, title)
    Surface(
        modifier = Modifier
            .height(36.dp)
            .semantics { contentDescription = description },
        onClick = onClick,
        shape = PillShape,
        color = if (isInstalled) InstalledMint.copy(alpha = 0.14f) else MoonlightPalette.Violet,
        contentColor = if (isInstalled) InstalledMint else Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(if (isInstalled) R.drawable.ic_check else R.drawable.ic_download_2_filled),
                contentDescription = null
            )
            Text(
                text = stringResource(if (isInstalled) R.string.moonlight_store_installed else R.string.moonlight_store_install),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** Placeholder card shown while a page loads (same size as a real card). */
@Composable
fun MoonlightProjectCardSkeleton(modifier: Modifier = Modifier) {
    val shimmer = MaterialTheme.colorScheme.surfaceContainerHighest
    Surface(
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(Modifier.size(56.dp), color = shimmer, shape = RoundedCornerShape(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(Modifier.width(120.dp).height(14.dp), color = shimmer, shape = RoundedCornerShape(6.dp))
                    ShimmerBox(Modifier.width(80.dp).height(10.dp), color = shimmer, shape = RoundedCornerShape(6.dp))
                    ShimmerBox(Modifier.width(60.dp).height(10.dp), color = shimmer, shape = RoundedCornerShape(6.dp))
                }
            }
            ShimmerBox(Modifier.fillMaxWidth().height(28.dp), color = shimmer, shape = RoundedCornerShape(6.dp))
            Row {
                Spacer(Modifier.weight(1f))
                ShimmerBox(Modifier.width(96.dp).height(36.dp), color = shimmer, shape = PillShape)
            }
        }
    }
}

/**
 * Filter panel that slides in from the right over the results, with a scrim
 * behind it. The system back gesture closes it.
 */
@Composable
fun BoxScope.MoonlightFilterSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)

    AnimatedVisibility(
        modifier = Modifier.matchParentSize(),
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
        visible = visible,
        enter = slideInHorizontally { it } + fadeIn(),
        exit = slideOutHorizontally { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(330.dp),
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 12.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.moonlight_store_filters),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClear) {
                        Text(text = stringResource(R.string.moonlight_store_clear))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.moonlight_store_close_filters)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    content()
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    onClick = onDismiss,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MoonlightPalette.Violet,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.moonlight_store_show_results),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
