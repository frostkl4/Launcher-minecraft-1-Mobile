/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 * Modifications Copyright (C) 2026 frostkl4 (Noctis Launcher home screen)
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.noctis.ui.home.NoctisAccountPanel
import com.movtery.zalithlauncher.noctis.ui.home.NoctisLibrary
import com.movtery.zalithlauncher.noctis.ui.home.NoctisPlayCard
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * Noctis home screen (landscape):
 *
 * ```
 * | rail | Play card (Play + selected instance | memory slider) | account  |
 * |      | Library: title, search, New instance                | + skin   |
 * |      | LazyRow of instance cards                           | preview  |
 * ```
 *
 * The parameters are the same as the original Zalith home, so MainScreen and the
 * rest of the app keep working unchanged.
 */
@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    @Suppress("UNUSED_PARAMETER") onQuickPlayServer: (Version?, String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenLink: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") startGuideOnce: (GuideKeys.Keys) -> Unit,
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

        val toAccountManager: () -> Unit = {
            backStackViewModel.mainScreen.navigateTo(
                screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
            )
        }
        val toNewInstance: () -> Unit = {
            backStackViewModel.navigateToDownload()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Right panel: about a quarter of the width, never too thin or too wide
            val panelWidth = (maxWidth * 0.26f).coerceIn(208.dp, 300.dp)
            // Play card grows a little on tall screens (tablets)
            val playCardHeight = (maxHeight * 0.28f).coerceIn(92.dp, 132.dp)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val playCtx = rememberAnimatedContext(isVisible = isVisible)
                    NoctisPlayCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(playCardHeight)
                            .animated(playCtx, from = AnimatedDirection.Top),
                        version = currentVersion,
                        isRefreshing = isRefreshing,
                        onPlay = { onLaunchGame(null) },
                        onOpenInstanceSettings = { version -> navigateToVersions(version) }
                    )

                    val libraryCtx = rememberAnimatedContext(isVisible = isVisible, delayMillis = 60)
                    NoctisLibrary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .animated(libraryCtx, from = AnimatedDirection.Bottom),
                        versions = versions,
                        currentVersion = currentVersion,
                        isRefreshing = isRefreshing,
                        onSelect = { version ->
                            if (version != currentVersion) VersionsManager.saveVersion(version)
                        },
                        onOpenSettings = { version -> navigateToVersions(version) },
                        onNewInstance = toNewInstance
                    )
                }

                val panelCtx = rememberAnimatedContext(isVisible = isVisible, delayMillis = 120)
                NoctisAccountPanel(
                    modifier = Modifier
                        .width(panelWidth)
                        .fillMaxHeight()
                        .animated(panelCtx, from = AnimatedDirection.Right),
                    account = account,
                    onOpenAccounts = toAccountManager,
                    onChangeSkin = toAccountManager
                )
            }
        }
    }
}
