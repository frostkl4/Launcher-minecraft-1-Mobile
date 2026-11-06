/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 * Modifications Copyright (C) 2026 frostkl4 (Moonlight Launcher home screen)
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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightHero
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightInstanceCard
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightLibraryHeader
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightLibraryMessage
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightLoaderFilters
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightMemoryDialog
import com.movtery.zalithlauncher.moonlight.ui.home.formatMoonlightMemory
import com.movtery.zalithlauncher.moonlight.ui.home.moonlightCategory
import com.movtery.zalithlauncher.moonlight.ui.home.rememberEffectiveMemoryMb
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/** Height of the "continue playing" card at the top of the home. */
private val HeroHeight = 168.dp

/** Cards are at least this wide; the grid adds columns as the screen gets wider. */
private val CardMinWidth = 150.dp

/**
 * Moonlight home (version 1.0, landscape):
 *
 * ```
 * | sidebar | top bar: "Home" · license line          [account chip] |
 * |         | hero: selected instance, Play, memory, quick actions, skin |
 * |         | Your instances: search · manage · New instance             |
 * |         | loader filters                                             |
 * |         | [card] [card] [card] [card]   ← rounded instance grid      |
 * ```
 *
 * Everything below the top bar is one lazy grid, so only the cards on screen are
 * composed and the hero scrolls away when browsing a long library.
 */
@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    @Suppress("UNUSED_PARAMETER") onQuickPlayServer: (Version?, String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenLink: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") startGuideOnce: (GuideKeys.Keys) -> Unit,
    onOpenFolder: (Version) -> Unit = {},
    onManageInstances: () -> Unit = {},
) {
    // The original first-run guide points at the old home layout, so it is not started here.

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        val versions by VersionsManager.versions.collectAsStateWithLifecycle()
        val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
        val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
        val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

        var query by rememberSaveable { mutableStateOf("") }
        var category by rememberSaveable { mutableStateOf<String?>(null) }
        var showMemoryDialog by remember { mutableStateOf(false) }

        // Loader chips only list what the user has, in library order
        val categories = remember(versions) { versions.map { it.moonlightCategory() }.distinct() }
        val activeCategory = category?.takeIf { it in categories }
        val trimmed = query.trim()
        val filtered = remember(versions, trimmed, activeCategory) {
            versions.asSequence()
                .filter { activeCategory == null || it.moonlightCategory() == activeCategory }
                .filter { trimmed.isEmpty() || it.getVersionName().contains(trimmed, ignoreCase = true) }
                .distinctBy { it.getVersionName() }
                .toList()
        }
        val currentName = currentVersion?.getVersionName()
        val memoryLabel = formatMoonlightMemory(rememberEffectiveMemoryMb(currentVersion))

        val toAccountManager: () -> Unit = {
            backStackViewModel.mainScreen.navigateTo(
                screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
            )
        }
        val toNewInstance: () -> Unit = {
            backStackViewModel.navigateToDownload()
        }
        val select: (Version) -> Unit = { version ->
            if (version.getVersionName() != currentName) VersionsManager.saveVersion(version)
        }

        val heroCtx = rememberAnimatedContext(isVisible = isVisible)
        val libraryCtx = rememberAnimatedContext(isVisible = isVisible, delayMillis = 60)
        val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyGridState(),
            columns = GridCells.Adaptive(minSize = CardMinWidth),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero", span = fullSpan, contentType = "hero") {
                MoonlightHero(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HeroHeight)
                        .animated(heroCtx, from = AnimatedDirection.Top),
                    version = currentVersion,
                    isRefreshing = isRefreshing,
                    account = account,
                    memoryLabel = memoryLabel,
                    onPlay = { onLaunchGame(null) },
                    onOpenMemory = { showMemoryDialog = true },
                    onInstanceSettings = navigateToVersions,
                    onOpenFolder = onOpenFolder,
                    onChangeSkin = toAccountManager
                )
            }

            item(key = "header", span = fullSpan, contentType = "header") {
                MoonlightLibraryHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(libraryCtx, from = AnimatedDirection.Bottom),
                    query = query,
                    onQueryChange = { query = it },
                    onManage = onManageInstances,
                    onNewInstance = toNewInstance
                )
            }

            if (categories.size > 1) {
                item(key = "filters", span = fullSpan, contentType = "filters") {
                    MoonlightLoaderFilters(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animated(libraryCtx, from = AnimatedDirection.Bottom),
                        categories = categories,
                        selected = activeCategory,
                        onSelect = { category = it }
                    )
                }
            }

            when {
                versions.isEmpty() && isRefreshing -> item(key = "message", span = fullSpan, contentType = "message") {
                    MoonlightLibraryMessage(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.moonlight_home_loading),
                        showProgress = true
                    )
                }
                versions.isEmpty() -> item(key = "message", span = fullSpan, contentType = "message") {
                    MoonlightLibraryMessage(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.moonlight_home_empty_library),
                        actionText = stringResource(R.string.moonlight_home_new_instance),
                        onAction = toNewInstance
                    )
                }
                filtered.isEmpty() -> item(key = "message", span = fullSpan, contentType = "message") {
                    MoonlightLibraryMessage(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.moonlight_home_no_results, trimmed)
                    )
                }
                else -> items(
                    items = filtered,
                    key = { "instance:" + it.getVersionName() },
                    contentType = { "instance" }
                ) { version ->
                    MoonlightInstanceCard(
                        modifier = Modifier.animateItem(),
                        version = version,
                        selected = version.getVersionName() == currentName,
                        onSelect = { select(version) },
                        onPlay = {
                            select(version)
                            onLaunchGame(version)
                        },
                        onSettings = { navigateToVersions(version) },
                        onOpenFolder = { onOpenFolder(version) }
                    )
                }
            }
        }

        if (showMemoryDialog) {
            MoonlightMemoryDialog(
                version = currentVersion,
                onDismiss = { showMemoryDialog = false }
            )
        }
    }
}
