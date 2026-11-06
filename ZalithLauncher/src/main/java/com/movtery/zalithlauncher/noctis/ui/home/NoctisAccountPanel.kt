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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.noctis.NoctisInfo
import com.movtery.zalithlauncher.ui.components.PetModelView
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Below this height the 3D skin preview is skipped (it would be too small to be useful). */
private val MinSkinPreviewHeight = 110.dp

/**
 * Right panel: current account, 3D skin preview, "Change skin" and the
 * "unofficial modified version" notice required by the upstream license.
 */
@Composable
fun NoctisAccountPanel(
    account: Account?,
    onOpenAccounts: () -> Unit,
    onChangeSkin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NoctisPanel(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccountChip(
                modifier = Modifier.fillMaxWidth(),
                account = account,
                onClick = onOpenAccounts
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(NoctisTokens.CardShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                if (maxHeight >= MinSkinPreviewHeight) {
                    SkinPreview(
                        modifier = Modifier.fillMaxSize(),
                        account = account
                    )
                }
            }

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                onClick = if (account != null) onChangeSkin else onOpenAccounts
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(
                        if (account != null) R.drawable.ic_checkroom else R.drawable.ic_add
                    ),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(
                        if (account != null) R.string.noctis_home_change_skin else R.string.noctis_home_add_account
                    ),
                    maxLines = 1
                )
            }

            // Required by Zalith Launcher 2's additional GPLv3 terms: modified versions
            // must say they are unofficial on the startup screen or main interface.
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${BuildKeys.LAUNCHER_NAME} ${BuildConfig.VERSION_NAME}\n" +
                        stringResource(R.string.noctis_unofficial_based_on, NoctisInfo.UPSTREAM_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccountChip(
    account: Account?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(NoctisTokens.InnerShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (account != null) {
            PlayerFace(
                account = account,
                avatarSize = 36.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_person_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account?.username ?: stringResource(R.string.noctis_home_no_account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (account != null) getAccountTypeName(account) else stringResource(R.string.noctis_home_add_account),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
            contentDescription = stringResource(R.string.noctis_home_accounts),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 3D preview of the account skin and cape (default skin when there is no account).
 * Skin files are read off the main thread.
 */
@Composable
private fun SkinPreview(
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

    // Panel is on the right, so the model turns a little towards the content on the left
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
        gazeEnabled = false
    )
}
