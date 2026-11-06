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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.NoctisPalette

/**
 * "Library" block: title, search, New instance button and a horizontal
 * list of instances. LazyRow only composes the cards that are on screen.
 */
@Composable
fun NoctisLibrary(
    versions: List<Version>,
    currentVersion: Version?,
    isRefreshing: Boolean,
    onSelect: (Version) -> Unit,
    onOpenSettings: (Version) -> Unit,
    onNewInstance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val trimmed = query.trim()
    val filtered = remember(versions, trimmed) {
        if (trimmed.isEmpty()) versions
        else versions.filter { it.getVersionName().contains(trimmed, ignoreCase = true) }
    }
    val currentName = currentVersion?.getVersionName()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = stringResource(R.string.noctis_home_library),
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
                onQueryChange = { query = it }
            )
            FilledTonalButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = onNewInstance,
                contentPadding = PaddingValues(horizontal = 14.dp)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                versions.isEmpty() && isRefreshing -> LibraryMessage(
                    text = stringResource(R.string.noctis_home_loading),
                    showProgress = true
                )
                versions.isEmpty() -> LibraryMessage(
                    text = stringResource(R.string.noctis_home_empty_library),
                    actionText = stringResource(R.string.noctis_home_new_instance),
                    onAction = onNewInstance
                )
                filtered.isEmpty() -> LibraryMessage(
                    text = stringResource(R.string.noctis_home_no_results, trimmed)
                )
                else -> LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(filtered, key = { it.getVersionName() }) { version ->
                        InstanceCard(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxHeight()
                                .aspectRatio(0.74f),
                            version = version,
                            selected = version.getVersionName() == currentName,
                            onClick = { onSelect(version) },
                            onLongClick = { onOpenSettings(version) }
                        )
                    }
                }
            }
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
    BasicTextField(
        modifier = modifier
            .clip(NoctisTokens.PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp),
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
                            text = stringResource(R.string.noctis_home_search),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                onQueryChange("")
                                focusManager.clearFocus()
                            }
                            .padding(5.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.noctis_home_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstanceCard(
    version: Version,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isValid = remember(version) { version.isValid() }
    val summary = remember(version, isValid) {
        if (isValid) runCatching { version.getVersionSummary() }.getOrNull() else null
    }
    val borderColor by animateColorAsState(
        if (selected) NoctisPalette.Violet else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "cardBorder"
    )
    val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp, label = "cardBorderWidth")

    Column(
        modifier = modifier
            .clip(NoctisTokens.CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .border(borderWidth, borderColor, NoctisTokens.CardShape)
            .combinedClickable(
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.noctis_home_instance_options),
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        // Cover: the instance icon (modpack icon when it has one) over a small night gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .clip(NoctisTokens.InnerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2E1F5C), Color(0xFF171029))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            VersionIconImage(
                modifier = Modifier.fillMaxSize(0.62f),
                version = version
            )

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
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = version.getVersionName(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary ?: "—",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LibraryMessage(
    text: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    NoctisPanel(
        modifier = modifier.fillMaxSize(),
        shape = NoctisTokens.CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
