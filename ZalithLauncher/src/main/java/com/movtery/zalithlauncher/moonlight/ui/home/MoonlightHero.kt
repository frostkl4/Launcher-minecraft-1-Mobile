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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.moonlight.ui.MoonlightSkyBackground
import com.movtery.zalithlauncher.ui.theme.MoonlightPalette

/**
 * Fixed "night" colors: the hero always sits on the night-sky art, so its text
 * colors do not follow the app theme (they would be unreadable on a light theme).
 */
private object HeroColors {
    val Shade = Color(0xFF0B0814)
    val Text = Color(0xFFE8E2F4)
    val TextMuted = Color(0xFFCAC3DA)
    val ButtonFill = Color(0xCC1A1526)
    val Line = Color(0x29CDB8FF)
}

private val HeroShape = RoundedCornerShape(22.dp)

/** Below this width the skin preview is dropped so the buttons keep their room. */
private val MinWidthForSkin = 460.dp

/**
 * Big "continue playing" card at the top of the home: selected instance, the
 * Play button, the memory chip, quick actions and the 3D skin on the right.
 */
@Composable
fun MoonlightHero(
    version: Version?,
    isRefreshing: Boolean,
    account: Account?,
    memoryLabel: String,
    onPlay: () -> Unit,
    onOpenMemory: () -> Unit,
    onInstanceSettings: (Version) -> Unit,
    onOpenFolder: (Version) -> Unit,
    onChangeSkin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(HeroShape)
            .border(BorderStroke(1.dp, HeroColors.Line), HeroShape)
    ) {
        val showSkin = maxWidth >= MinWidthForSkin

        MoonlightSkyBackground(modifier = Modifier.fillMaxSize())
        // Dark fade on the text side so the title always reads over the art
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to HeroColors.Shade.copy(alpha = 0.92f),
                        0.5f to HeroColors.Shade.copy(alpha = 0.55f),
                        0.78f to Color.Transparent
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroInfo(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                version = version,
                isRefreshing = isRefreshing,
                memoryLabel = memoryLabel,
                onPlay = onPlay,
                onOpenMemory = onOpenMemory,
                onInstanceSettings = onInstanceSettings,
                onOpenFolder = onOpenFolder
            )

            if (showSkin) {
                Column(
                    modifier = Modifier
                        .width(112.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MoonlightSkinPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        account = account
                    )
                    Surface(
                        onClick = onChangeSkin,
                        shape = RoundedCornerShape(12.dp),
                        color = HeroColors.Shade.copy(alpha = 0.7f),
                        contentColor = MoonlightPalette.Lavender,
                        border = BorderStroke(1.dp, HeroColors.Line)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            text = stringResource(
                                if (account != null) R.string.moonlight_home_change_skin else R.string.moonlight_home_add_account
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroInfo(
    version: Version?,
    isRefreshing: Boolean,
    memoryLabel: String,
    onPlay: () -> Unit,
    onOpenMemory: () -> Unit,
    onInstanceSettings: (Version) -> Unit,
    onOpenFolder: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when {
        version != null -> version.getVersionName()
        isRefreshing -> stringResource(R.string.moonlight_home_loading)
        else -> stringResource(R.string.moonlight_home_no_instance)
    }
    val tags = remember(version) { version?.moonlightTags().orEmpty() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.moonlight_home_continue).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MoonlightPalette.Lavender,
            maxLines = 1
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HeroColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (tags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    MoonlightTag(
                        text = tag,
                        color = HeroColors.Text,
                        background = Color.White.copy(alpha = 0.12f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayPill(onClick = onPlay)

            val memoryDescription = stringResource(R.string.moonlight_home_memory_button, memoryLabel)
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .semantics { contentDescription = memoryDescription },
                onClick = onOpenMemory,
                shape = RoundedCornerShape(22.dp),
                color = HeroColors.ButtonFill,
                contentColor = MoonlightPalette.Lavender,
                border = BorderStroke(1.dp, HeroColors.Line)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_moonlight_memory),
                        contentDescription = null
                    )
                    Text(
                        text = memoryLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            if (version != null) {
                HeroIconButton(
                    icon = R.drawable.ic_moonlight_settings,
                    label = stringResource(R.string.moonlight_home_instance_options),
                    onClick = { onInstanceSettings(version) }
                )
                HeroIconButton(
                    icon = R.drawable.ic_moonlight_folder,
                    label = stringResource(R.string.moonlight_home_open_folder),
                    onClick = { onOpenFolder(version) }
                )
            }
        }
    }
}

@Composable
private fun PlayPill(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val container by animateColorAsState(
        if (pressed) MoonlightPalette.VioletPressed else MoonlightPalette.Violet,
        label = "playColor"
    )
    Surface(
        modifier = Modifier.height(48.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = container,
        contentColor = Color.White,
        shadowElevation = 6.dp,
        interactionSource = interaction
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_moonlight_play),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.moonlight_home_play),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroIconButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    size: Dp = 44.dp,
) {
    Surface(
        modifier = Modifier
            .size(size)
            .semantics { contentDescription = label },
        onClick = onClick,
        shape = CircleShape,
        color = HeroColors.ButtonFill,
        contentColor = HeroColors.TextMuted,
        border = BorderStroke(1.dp, HeroColors.Line)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
        }
    }
}
