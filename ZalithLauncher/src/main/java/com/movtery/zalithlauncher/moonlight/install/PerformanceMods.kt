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

package com.movtery.zalithlauncher.moonlight.install

import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersions
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.game.GameExtraMod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Loaders offered by the Moonlight installer. */
enum class InstallLoader(val displayName: String) {
    VANILLA("Vanilla"),
    FABRIC("Fabric"),
    FORGE("Forge"),
    NEOFORGE("NeoForge"),
    QUILT("Quilt");

    /** Modrinth loader ids whose builds run on this loader, best first. */
    val modrinthLoaders: List<String>
        get() = when (this) {
            FABRIC -> listOf("fabric")
            QUILT -> listOf("quilt", "fabric")
            NEOFORGE -> listOf("neoforge")
            VANILLA, FORGE -> emptyList()
        }

    /** Sodium and Lithium only exist for these loaders. */
    val supportsPerformanceMods: Boolean get() = modrinthLoaders.isNotEmpty()
}

/**
 * Performance mods put in a new instance: Sodium (rendering, much higher FPS)
 * and Lithium (game logic, server tick). Both are on Modrinth, open source,
 * and have no required dependencies on Fabric, Quilt and NeoForge.
 */
object PerformanceMods {
    private object Sodium : ModVersions("AANobbMI")
    private object Lithium : ModVersions("gvQqBUqZ")

    private val mods = listOf("Sodium" to Sodium, "Lithium" to Lithium)

    /** Names of the mods, for the UI. */
    val names: List<String> = mods.map { it.first }

    /**
     * Newest build of each mod for this Minecraft version and loader
     * (release builds first, beta only if there is no release).
     * Mods without a compatible build are left out; network errors leave a mod out too.
     */
    suspend fun resolve(mcVersion: String, loader: InstallLoader): List<GameExtraMod> {
        val loaders = loader.modrinthLoaders
        if (loaders.isEmpty()) return emptyList()

        return coroutineScope {
            mods.map { (name, source) ->
                async {
                    val versions = try {
                        source.fetchVersionList(mcVersion).orEmpty()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        emptyList()
                    }
                    pick(versions, loaders)?.let { GameExtraMod(name = name, version = it) }
                }
            }.mapNotNull { it.await() }
        }
    }

    private fun pick(versions: List<ModVersion>, loaders: List<String>): ModVersion? {
        // Modrinth lists versions newest first
        return loaders.firstNotNullOfOrNull { loaderId ->
            val forLoader = versions.filter { it.version.loaders.contains(loaderId) }
            forLoader.firstOrNull { it.version.versionType == PlatformReleaseType.RELEASE }
                ?: forLoader.firstOrNull()
        }
    }
}
