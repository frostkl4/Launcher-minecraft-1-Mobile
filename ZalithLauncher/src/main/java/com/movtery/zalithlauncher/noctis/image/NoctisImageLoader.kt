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

package com.movtery.zalithlauncher.noctis.image

import android.app.ActivityManager
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.movtery.zalithlauncher.path.IMAGE_LOADER_CLIENT
import com.movtery.zalithlauncher.path.PathManager
import okio.Path.Companion.toOkioPath

/**
 * The one image loader of the launcher (Coil 3): project icons from Modrinth and
 * CurseForge, skins, screenshots, instance icons.
 *
 * How an icon is shown:
 * 1. **Memory cache**: decoded bitmaps of what was seen recently. Scrolling back up
 *    shows them instantly, with no decoding and no network.
 * 2. **Disk cache** (`DIR_IMAGE_CACHE`): every downloaded file is kept on disk and is
 *    reused as it is on the next opening of the launcher, so the same icon is
 *    downloaded only once. Coil 3 does not revalidate cached files by default,
 *    which is what we want for project icons (they rarely change).
 * 3. **Network**, only when the icon is in neither cache. Downloads and decoding run on
 *    Coil's background dispatchers; the UI thread only draws the finished bitmap.
 *
 * Every request is sized to the slot it fills ([iconRequest]), so a 72 dp icon is
 * decoded at about 216 px instead of the full 512-1024 px original.
 */
object NoctisImageLoader {
    /** Share of the app memory class kept for decoded bitmaps. */
    private const val MEMORY_PERCENT = 0.20
    private const val MEMORY_PERCENT_LOW_RAM = 0.10

    /** Disk cache: 2% of the free space, never under 64 MB nor over 384 MB. */
    private const val DISK_PERCENT = 0.02
    private const val DISK_MIN_BYTES = 64L * 1024 * 1024
    private const val DISK_MAX_BYTES = 384L * 1024 * 1024

    fun create(context: Context): ImageLoader {
        val lowRam = (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.isLowRamDevice == true

        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, if (lowRam) MEMORY_PERCENT_LOW_RAM else MEMORY_PERCENT)
                    // Bitmaps pushed out of the strong cache stay reachable while still on screen
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(PathManager.DIR_IMAGE_CACHE.toOkioPath())
                    .maxSizePercent(DISK_PERCENT)
                    .minimumMaxSizeBytes(DISK_MIN_BYTES)
                    .maximumMaxSizeBytes(DISK_MAX_BYTES)
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .components {
                add(KtorNetworkFetcherFactory(IMAGE_LOADER_CLIENT))
                add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }

    /**
     * Request for a square project icon of [sizePx] pixels.
     * Decoding at the slot size keeps both memory and decode time small.
     */
    fun iconRequest(context: PlatformContext, url: String, sizePx: Int): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .size(sizePx)
            .crossfade(true)
            .build()
    }
}
