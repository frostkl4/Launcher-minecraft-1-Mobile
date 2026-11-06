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

package com.movtery.zalithlauncher.game.account.wardrobe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.path.createOkHttpClientBuilder
import com.movtery.zalithlauncher.path.createRequestBuilder
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.Base64

private const val TAG = "OfflineCapes"

/*
 * How capes reach the game for an OFFLINE account
 * ------------------------------------------------
 * Offline accounts have no Mojang profile, so the game has nowhere to download a cape from.
 * The launcher already runs a tiny local Yggdrasil server (OfflineYggdrasilServer) and starts
 * the game with authlib-injector pointed at it:
 *
 *   -javaagent:authlib-injector.jar=http://localhost:<port>
 *
 * When the game asks for the player's textures, that server answers with SKIN and CAPE urls that
 * point back to itself (/textures/<sha256>), and serves the PNG bytes saved in Account.getCapeFile().
 * So "injecting the cape" = saving a valid cape PNG to that file. This file only fetches/validates it.
 *
 * Microsoft accounts do not need any of this: the game downloads the active official cape from
 * Mojang's session server by itself, and the launcher already lists/changes the owned capes.
 */

/** Where an offline cape can come from. */
enum class OfflineCapeSource { FILE, ELY_BY, MINECRAFT_CAPES }

/** A cape could not be obtained; [reason] says why, for the UI. */
class OfflineCapeException(val reason: Reason, message: String, cause: Throwable? = null) :
    IOException(message, cause) {
    enum class Reason {
        /** The player exists but has no cape on that service. */
        NO_CAPE,
        /** The username does not exist on that service / at Mojang. */
        PLAYER_NOT_FOUND,
        /** The image is not a usable cape texture. */
        INVALID_IMAGE,
        /** Network or server problem. */
        NETWORK
    }
}

private val capeHttpClient by lazy { createOkHttpClientBuilder().build() }

private class HttpResult(val code: Int, val body: ByteArray)

private suspend fun httpGet(url: String): HttpResult = withContext(Dispatchers.IO) {
    try {
        runInterruptible {
            capeHttpClient.newCall(createRequestBuilder(url).build()).execute().use { response ->
                HttpResult(response.code, response.body.bytes())
            }
        }
    } catch (e: IOException) {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "Request failed: $url", e)
    }
}

private fun String.urlPath(): String = URLEncoder.encode(trim(), "UTF-8").replace("+", "%20")

/**
 * Ely.by skin system.
 * GET https://skinsystem.ely.by/textures/{username}
 *  - 200 {"SKIN": {...}, "CAPE": {"url": "..."}}  (CAPE is missing when the player has none)
 *  - 204 when the username has no textures at all
 */
suspend fun fetchElyByCape(username: String): ByteArray {
    val result = httpGet("https://skinsystem.ely.by/textures/${username.urlPath()}")
    if (result.code == 204 || result.code == 404) {
        throw OfflineCapeException(OfflineCapeException.Reason.PLAYER_NOT_FOUND, "Ely.by: no textures for $username")
    }
    if (result.code != 200) {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "Ely.by answered HTTP ${result.code}")
    }
    val capeUrl = runCatching {
        JsonParser.parseString(result.body.decodeToString()).asJsonObject
            .getAsJsonObject("CAPE")?.get("url")?.asString
    }.getOrNull() ?: throw OfflineCapeException(OfflineCapeException.Reason.NO_CAPE, "Ely.by: $username has no cape")

    // Ely.by still hands out http:// links; the same files are served over https
    val secureUrl = capeUrl.replaceFirst(Regex("^http://(?=([a-z0-9-]+\\.)*ely\\.by/)"), "https://")
    val image = httpGet(secureUrl)
    if (image.code != 200) {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "Ely.by cape download HTTP ${image.code}")
    }
    return normalizeCapeImage(image.body, animated = false)
}

/**
 * Mojang profile lookup: username -> UUID (without dashes).
 * GET https://api.mojang.com/users/profiles/minecraft/{username}
 */
suspend fun fetchMojangUuid(username: String): String {
    val result = httpGet("https://api.mojang.com/users/profiles/minecraft/${username.urlPath()}")
    if (result.code == 404 || result.code == 204) {
        throw OfflineCapeException(OfflineCapeException.Reason.PLAYER_NOT_FOUND, "Mojang: $username not found")
    }
    if (result.code != 200) {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "Mojang answered HTTP ${result.code}")
    }
    return runCatching {
        JsonParser.parseString(result.body.decodeToString()).asJsonObject.get("id").asString
    }.getOrElse {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "Mojang: unexpected response", it)
    }
}

/**
 * MinecraftCapes (minecraftcapes.net). Capes are tied to a Mojang (premium) UUID, so the
 * username is resolved at Mojang first.
 * GET https://api.minecraftcapes.net/profile/{uuid}
 *  - {"animatedCape": bool, "cape_url": "https://..." | null, "textures": {"cape": "<base64>" | null, ...}}
 * The base64 "textures" field is being phased out in favour of "cape_url"; both are handled.
 */
suspend fun fetchMinecraftCapesCape(username: String): ByteArray {
    val uuid = fetchMojangUuid(username)
    val result = httpGet("https://api.minecraftcapes.net/profile/$uuid")
    if (result.code == 404) {
        throw OfflineCapeException(OfflineCapeException.Reason.NO_CAPE, "MinecraftCapes: no profile for $username")
    }
    if (result.code != 200) {
        throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "MinecraftCapes answered HTTP ${result.code}")
    }
    val json = runCatching { JsonParser.parseString(result.body.decodeToString()).asJsonObject }
        .getOrElse { throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "MinecraftCapes: bad JSON", it) }

    val animated = json.get("animatedCape")?.takeIf { !it.isJsonNull }?.asBoolean ?: false
    val capeUrl = json.get("cape_url")?.takeIf { !it.isJsonNull }?.asString
    val base64 = json.get("textures")?.takeIf { it.isJsonObject }?.asJsonObject
        ?.get("cape")?.takeIf { !it.isJsonNull }?.asString

    val bytes = when {
        !capeUrl.isNullOrBlank() -> {
            val image = httpGet(capeUrl)
            if (image.code != 200) {
                throw OfflineCapeException(OfflineCapeException.Reason.NETWORK, "MinecraftCapes cape download HTTP ${image.code}")
            }
            image.body
        }
        !base64.isNullOrBlank() -> Base64.getDecoder().decode(base64)
        else -> throw OfflineCapeException(OfflineCapeException.Reason.NO_CAPE, "MinecraftCapes: $username has no cape")
    }
    return normalizeCapeImage(bytes, animated)
}

/** Largest cape width kept as is; bigger (HD) capes are scaled down to save memory. */
private const val MAX_CAPE_WIDTH = 512

/**
 * Turns any reasonable cape image into a texture the game understands (width = 2 x height):
 *  - 64x32 (and 128x64, 256x128, ...) are kept;
 *  - the old 22x17 cape format is placed on a 64x32 canvas;
 *  - animated capes (frames stacked vertically) keep only the first frame;
 *  - anything wider than [MAX_CAPE_WIDTH] is scaled down.
 * @throws OfflineCapeException with [OfflineCapeException.Reason.INVALID_IMAGE] otherwise
 */
fun normalizeCapeImage(bytes: ByteArray, animated: Boolean = false): ByteArray {
    val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw OfflineCapeException(OfflineCapeException.Reason.INVALID_IMAGE, "Not an image")
    val w = source.width
    val h = source.height

    val frame: Bitmap = when {
        // Legacy 22x17 cape layout -> standard 64x32 canvas
        w == 22 && h == 17 -> {
            Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888).also { canvas ->
                Canvas(canvas).drawBitmap(source, 0f, 0f, null)
            }
        }
        w >= 64 && w % 64 == 0 && h * 2 == w -> source
        // Animated sheet or taller-than-standard image: take the top frame
        w >= 64 && w % 64 == 0 && h > w / 2 && (animated || h % (w / 2) == 0) ->
            Bitmap.createBitmap(source, 0, 0, w, w / 2)
        else -> throw OfflineCapeException(
            OfflineCapeException.Reason.INVALID_IMAGE,
            "Unsupported cape size ${w}x$h (expected 64x32 or a multiple)"
        )
    }

    val scaled = if (frame.width > MAX_CAPE_WIDTH) {
        Bitmap.createScaledBitmap(frame, MAX_CAPE_WIDTH, MAX_CAPE_WIDTH / 2, false) // nearest neighbour keeps pixels sharp
    } else frame

    return ByteArrayOutputStream().use { out ->
        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }.also {
        if (scaled !== frame) scaled.recycle()
        if (frame !== source) frame.recycle()
        source.recycle()
    }
}

/**
 * Saves (or removes, when [capeBytes] is null) the cape of an offline account.
 * The local Yggdrasil server picks it up on the next game launch.
 */
suspend fun applyOfflineCape(account: Account, capeBytes: ByteArray?) = withContext(Dispatchers.IO) {
    val file = account.getCapeFile()
    if (capeBytes == null) {
        if (file.exists() && !file.delete()) Logger.warning(TAG, "Could not delete cape of ${account.username}")
        Logger.info(TAG, "Removed offline cape of ${account.username}")
    } else {
        file.parentFile?.mkdirs()
        file.writeBytes(capeBytes)
        Logger.info(TAG, "Saved offline cape of ${account.username} (${capeBytes.size} bytes)")
    }
}
