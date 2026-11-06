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

package com.movtery.zalithlauncher.moonlight.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.ui.components.PetModelView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 3D preview of the account skin and cape (default skin when there is no account).
 * Skin files are read off the main thread.
 */
@Composable
fun MoonlightSkinPreview(
    account: Account?,
    modifier: Modifier = Modifier,
) {
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val armModel = account?.skinModelType?.toArmModel()
    val skinStamp = account?.getSkinFile()?.lastModified()
    val capeStamp = account?.getCapeFile()?.lastModified()

    val skinBytes by produceState<ByteArray?>(null, account, refreshWardrobe, skinStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getSkinFile()?.takeIf { it.exists() }?.inputStream()?.use { it.readBytes() }
        }
    }
    val capeBytes by produceState<ByteArray?>(null, account, refreshWardrobe, capeStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getCapeFile()?.takeIf { it.exists() }?.inputStream()?.use { it.readBytes() }
        }
    }

    // The preview sits on the right of the hero, so the model turns towards the text
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val yaw = remember(isRtl) {
        Math.toRadians(if (isRtl) 30.0 else -30.0).toFloat()
    }

    PetModelView(
        modifier = modifier,
        skin = skinBytes,
        cape = capeBytes,
        arm = armModel,
        yaw = yaw,
        animation = PetAnimation.Idle,
        tapReaction = PetTapReaction.Interact,
        dragYaw = true,
        gazeEnabled = false,
        // Idle breathing looks the same at 30 fps and costs half the CPU of 60
        maxFps = 30
    )
}
