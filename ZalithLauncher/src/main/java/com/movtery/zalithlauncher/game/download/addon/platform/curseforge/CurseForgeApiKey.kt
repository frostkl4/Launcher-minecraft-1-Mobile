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

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.setting.AllSettings
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

/**
 * The official CurseForge API answers every request without a valid key with
 * "403 Forbidden: API Key missing or invalid". The key comes from Settings → Launcher
 * or, as a fallback, from the CURSEFORGE_API_KEY repository secret at build time.
 */
class CurseForgeApiKeyMissingException :
    IllegalStateException("CurseForge API key is not set")

/**
 * Key used for CurseForge requests: the one pasted in Settings → Launcher wins,
 * otherwise the one compiled in from the CURSEFORGE_API_KEY secret (may be empty).
 */
fun currentCurseForgeApiKey(): String =
    AllSettings.curseForgeApiKey.getValue().trim().ifBlank { BuildKeys.CURSEFORGE_API }

/** True when there is no CurseForge API key at all. */
val isCurseForgeApiKeyMissing: Boolean
    get() = currentCurseForgeApiKey().isBlank()

/**
 * Fails fast with a clear error instead of sending a request that can only return 403.
 * Mirrors (MCIM) do not need the key, so only the official API is checked.
 */
fun requireCurseForgeApiKey(api: String) {
    if (api == CURSEFORGE_API && isCurseForgeApiKeyMissing) {
        throw CurseForgeApiKeyMissingException()
    }
}

/** True when the official CurseForge API refused the key (403). */
fun Throwable.isCurseForgeKeyRejected(): Boolean {
    if (this !is ResponseException) return false
    return response.status == HttpStatusCode.Forbidden &&
            response.call.request.url.host == "api.curseforge.com"
}

/**
 * CurseForge is useless in a build without a key, so screens that can switch
 * platforms open on Modrinth instead of showing an error first.
 */
fun Platform.usableOrModrinth(): Platform =
    if (this == Platform.CURSEFORGE && isCurseForgeApiKeyMissing) Platform.MODRINTH else this
