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

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.NoctisPalette
import java.util.Random
import kotlin.math.ceil
import kotlin.math.max

/** Filter key for instances without a mod loader. */
const val NOCTIS_VANILLA_KEY = "vanilla"

/** Loader name used by the filter chips ("Forge", "Fabric"…), or [NOCTIS_VANILLA_KEY]. */
fun Version.noctisCategory(): String {
    return getVersionInfo()?.primaryLoader?.loader?.displayName?.takeIf { it.isNotBlank() }
        ?: NOCTIS_VANILLA_KEY
}

/** Short tags shown on cards and on the hero: Minecraft version and the main loader. */
fun Version.noctisTags(): List<String> {
    val info = getVersionInfo() ?: return emptyList()
    return listOfNotNull(
        info.minecraftVersion.takeIf { it.isNotBlank() },
        info.primaryLoader?.loader?.displayName?.takeIf { it.isNotBlank() }
    )
}

private val CardShape = RoundedCornerShape(18.dp)
private val CoverShape = RoundedCornerShape(12.dp)

@Composable
fun NoctisTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    background: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1
    )
}

/**
 * "Your instances" row: title, search, manage and New instance.
 */
@Composable
fun NoctisLibraryHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onManage: () -> Unit,
    onNewInstance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = stringResource(R.string.noctis_home_your_instances),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        SearchField(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            query = query,
            onQueryChange = onQueryChange
        )
        IconButton(
            modifier = Modifier.size(44.dp),
            onClick = onManage
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.ic_noctis_library),
                contentDescription = stringResource(R.string.noctis_home_manage),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FilledTonalButton(
            modifier = Modifier.fillMaxHeight(),
            onClick = onNewInstance,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = stringResource(R.string.noctis_home_new_instance),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val placeholder = stringResource(R.string.noctis_home_search)
    BasicTextField(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp)
            .semantics { contentDescription = placeholder },
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(NoctisPalette.Lavender),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                onQueryChange("")
                                focusManager.clearFocus()
                            }
                            .padding(7.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.noctis_home_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

/**
 * Loader filter chips ("All", "Forge", "Fabric", "Vanilla"…). Only the loaders
 * the user actually has are listed.
 */
@Composable
fun NoctisLoaderFilters(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoaderChip(
            label = stringResource(R.string.noctis_home_filter_all),
            selected = selected == null,
            onClick = { onSelect(null) }
        )
        categories.forEach { key ->
            LoaderChip(
                label = if (key == NOCTIS_VANILLA_KEY) stringResource(R.string.noctis_home_vanilla) else key,
                selected = selected == key,
                onClick = { onSelect(key) }
            )
        }
    }
}

@Composable
private fun LoaderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, maxLines = 1) },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            selectedLabelColor = MaterialTheme.colorScheme.secondary
        )
    )
}

/**
 * Rounded instance card: art cover with the instance icon (the modpack icon when it
 * came from a modpack), name, version tags and a round Play button.
 *
 * Tap selects, the round button plays, long press opens the actions menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoctisInstanceCard(
    version: Version,
    selected: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = version.getVersionName()
    val isValid = remember(version) { version.isValid() }
    val tags = remember(version) { version.noctisTags() }
    var menuOpen by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        if (selected) NoctisPalette.Violet else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "cardBorder"
    )
    val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp, label = "cardBorderWidth")

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(borderWidth, borderColor, CardShape)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.noctis_home_select),
                    onLongClickLabel = stringResource(R.string.noctis_home_instance_options),
                    onLongClick = { menuOpen = true },
                    onClick = onSelect
                )
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(CoverShape)
            ) {
                InstanceCoverArt(
                    modifier = Modifier.fillMaxSize(),
                    seed = name
                )

                // Instance / modpack icon on a small dark plate
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xB30B0814))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VersionIconImage(
                        modifier = Modifier.fillMaxSize(),
                        version = version
                    )
                }

                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(NoctisPalette.Violet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(R.string.noctis_home_selected),
                            tint = Color.White
                        )
                    }
                }

                if (!isValid) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(18.dp),
                        painter = painterResource(R.drawable.ic_warning_outlined),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    onClick = onPlay,
                    enabled = isValid,
                    shape = CircleShape,
                    color = NoctisPalette.Violet,
                    contentColor = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_noctis_play),
                            contentDescription = stringResource(R.string.noctis_home_play_instance, name)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.heightIn(min = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { NoctisTag(text = it) }
                }
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            shape = RoundedCornerShape(14.dp)
        ) {
            CardMenuItem(R.drawable.ic_noctis_play, stringResource(R.string.noctis_home_play), enabled = isValid) {
                menuOpen = false
                onPlay()
            }
            CardMenuItem(R.drawable.ic_noctis_settings, stringResource(R.string.noctis_home_instance_options)) {
                menuOpen = false
                onSettings()
            }
            CardMenuItem(R.drawable.ic_noctis_folder, stringResource(R.string.noctis_home_open_folder)) {
                menuOpen = false
                onOpenFolder()
            }
        }
    }
}

@Composable
private fun CardMenuItem(
    @DrawableRes icon: Int,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        enabled = enabled,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
        }
    )
}

private class CoverTint(val sky: Color, val mountain: Color, val glow: Color)

private val CoverTints = listOf(
    CoverTint(Color(0xFF2B1B57), Color(0xFF4B2F94), Color(0xFFE9DDFF)), // violet
    CoverTint(Color(0xFF102B33), Color(0xFF1E5560), Color(0xFFA8F0E6)), // teal
    CoverTint(Color(0xFF33160F), Color(0xFF6E2B1D), Color(0xFFFFC9A8)), // ember
    CoverTint(Color(0xFF13281A), Color(0xFF2A5236), Color(0xFFC9F2B8)), // forest
    CoverTint(Color(0xFF331329), Color(0xFF6A2756), Color(0xFFFFC6EC)), // rose
)

/** Width of the cover in "pixels" of pixel art. */
private const val COVER_COLUMNS = 32

/**
 * Pixel-art night landscape generated from the instance name: every instance gets
 * its own colors and mountains, with no image files, no decoding and no memory
 * beyond a couple of paths. Rebuilt only when the size or the name changes.
 */
@Composable
private fun InstanceCoverArt(
    seed: String,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier.drawWithCache {
            val hash = seed.hashCode()
            val tint = CoverTints[Math.floorMod(hash, CoverTints.size)]
            val random = Random(hash.toLong())

            val w = size.width
            val h = size.height
            val cell = w / COVER_COLUMNS
            val rows = max(4, ceil(h / cell).toInt())

            // Far ridge and a darker, lower near ridge, as step outlines (no seams between columns)
            val far = Path()
            val near = Path()
            far.moveTo(0f, h)
            near.moveTo(0f, h)
            var height = rows / 2
            for (x in 0 until COVER_COLUMNS) {
                height = (height + random.nextInt(5) - 2).coerceIn(rows / 4, rows * 3 / 4)
                val nearHeight = max(1, height - 3 - random.nextInt(2))
                val left = x * cell
                val right = left + cell
                far.lineTo(left, h - height * cell)
                far.lineTo(right, h - height * cell)
                near.lineTo(left, h - nearHeight * cell)
                near.lineTo(right, h - nearHeight * cell)
            }
            far.lineTo(w, h)
            far.close()
            near.lineTo(w, h)
            near.close()

            val stars = List(6) {
                Offset(random.nextInt(COVER_COLUMNS) * cell, random.nextInt(max(1, rows / 3)) * cell)
            }
            val moon = Offset(
                (COVER_COLUMNS * (0.62f + random.nextFloat() * 0.25f)).toInt() * cell,
                cell * (1 + random.nextInt(2))
            )
            val sky = Brush.verticalGradient(listOf(tint.sky, lerp(tint.sky, Color.Black, 0.45f)))
            val cellSize = Size(cell, cell)
            val nearColor = Color.Black.copy(alpha = 0.38f)

            onDrawBehind {
                drawRect(sky)
                stars.forEach { drawRect(tint.glow.copy(alpha = 0.55f), it, cellSize) }
                // Blocky moon: a 5x5 square with the corners cut
                drawRect(tint.glow, moon + Offset(cell, 0f), Size(cell * 3, cell * 5))
                drawRect(tint.glow, moon + Offset(0f, cell), Size(cell * 5, cell * 3))
                drawPath(far, tint.mountain)
                drawPath(near, nearColor)
            }
        }
    )
}

/**
 * Full-width message for the grid (loading, empty library, nothing found).
 */
@Composable
fun NoctisLibraryMessage(
    text: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    NoctisPanel(
        modifier = modifier,
        shape = NoctisTokens.CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionText != null) {
                FilledTonalButton(onClick = onAction) {
                    Text(text = actionText)
                }
            }
        }
    }
}
