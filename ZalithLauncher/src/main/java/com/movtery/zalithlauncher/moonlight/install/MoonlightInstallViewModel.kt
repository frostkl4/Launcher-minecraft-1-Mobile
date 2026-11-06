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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameExtraMod
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.isType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/** Minecraft version list of the installer. */
sealed interface VersionListState {
    data object Loading : VersionListState
    data class Ready(val versions: List<MinecraftVersion>) : VersionListState
    data object Failed : VersionListState
}

/** One loader build offered in the version dropdown. */
class LoaderChoice(
    val label: String,
    val recommended: Boolean,
    val value: Any,
)

/** Loader builds for the selected Minecraft version. */
sealed interface LoaderListState {
    data object Loading : LoaderListState
    data class Ready(val choices: List<LoaderChoice>) : LoaderListState
    data object Empty : LoaderListState
    data object Failed : LoaderListState
}

/** Performance mods found for the selected version and loader. */
sealed interface PerformanceState {
    data object Loading : PerformanceState
    data class Ready(val mods: List<GameExtraMod>) : PerformanceState
}

/**
 * State of the Moonlight version installer: loader type, Minecraft version list,
 * and for the selected version the loader build, the loader API mod (Fabric API /
 * Quilted Fabric API) and the performance mods. Everything comes from the same
 * sources the original installer uses (Mojang manifest, Fabric/Quilt meta,
 * Forge/NeoForge maven, Modrinth).
 */
class MoonlightInstallViewModel : ViewModel() {
    var loader by mutableStateOf(InstallLoader.FABRIC)
        private set
    var showSnapshots by mutableStateOf(false)
        private set
    var query by mutableStateOf("")
    var versionState by mutableStateOf<VersionListState>(VersionListState.Loading)
        private set

    var selected by mutableStateOf<MinecraftVersion?>(null)
        private set
    var loaderState by mutableStateOf<LoaderListState>(LoaderListState.Loading)
        private set
    var loaderChoice by mutableStateOf<LoaderChoice?>(null)
    var apiMod by mutableStateOf<ModVersion?>(null)
        private set
    var includeApi by mutableStateOf(true)
    var performanceState by mutableStateOf<PerformanceState>(PerformanceState.Loading)
        private set
    var includePerformance by mutableStateOf(true)

    /** Instance name; follows the selection until the user edits it. */
    var name by mutableStateOf("")
        private set
    private var nameEditedByUser = false

    private var selectionJob: Job? = null

    init {
        loadVersions(force = false)
    }

    fun loadVersions(force: Boolean) {
        viewModelScope.launch {
            versionState = VersionListState.Loading
            versionState = try {
                MinecraftVersions.refreshVersions(force)
                VersionListState.Ready(MinecraftVersions.allVersions.value)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                VersionListState.Failed
            }
        }
    }

    /** Versions shown for the current loader, snapshot switch and search text. */
    fun visibleVersions(all: List<MinecraftVersion>): List<MinecraftVersion> {
        val text = query.trim()
        return all.filter { version ->
            version.isType(release = true, snapshot = showSnapshots, aprilFools = false, old = false) &&
                    loaderSupports(version.version.id) &&
                    (text.isEmpty() || version.version.id.contains(text, ignoreCase = true))
        }
    }

    private fun loaderSupports(id: String): Boolean = when (loader) {
        InstallLoader.VANILLA, InstallLoader.FORGE -> true
        InstallLoader.FABRIC, InstallLoader.QUILT -> id.isBiggerVer("1.13.2")
        InstallLoader.NEOFORGE -> id.isBiggerVer("1.20")
    }

    fun changeLoader(newLoader: InstallLoader) {
        if (newLoader == loader) return
        loader = newLoader
        val current = selected
        if (current != null && loaderSupports(current.version.id)) {
            select(current)
        } else {
            clearSelection()
        }
    }

    fun changeSnapshots(show: Boolean) {
        showSnapshots = show
    }

    fun editName(value: String) {
        name = value
        nameEditedByUser = true
    }

    fun chooseLoader(choice: LoaderChoice) {
        loaderChoice = choice
        refreshAutoName()
    }

    fun clearSelection() {
        selectionJob?.cancel()
        selected = null
    }

    /** Selects a Minecraft version and looks up loader builds and mods for it. */
    fun select(version: MinecraftVersion) {
        selectionJob?.cancel()
        selected = version
        nameEditedByUser = false
        loaderChoice = null
        apiMod = null
        includeApi = true
        includePerformance = true
        val mcVersion = version.version.id
        val currentLoader = loader
        refreshAutoName()

        if (currentLoader == InstallLoader.VANILLA) {
            loaderState = LoaderListState.Ready(emptyList())
            performanceState = PerformanceState.Ready(emptyList())
            return
        }

        loaderState = LoaderListState.Loading
        performanceState = PerformanceState.Loading

        selectionJob = viewModelScope.launch {
            val performance = async { PerformanceMods.resolve(mcVersion, currentLoader) }
            val api = async { fetchApiMod(mcVersion, currentLoader) }

            loaderState = try {
                val choices = fetchLoaderChoices(mcVersion, currentLoader)
                if (choices.isEmpty()) LoaderListState.Empty else LoaderListState.Ready(choices)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                LoaderListState.Failed
            }
            (loaderState as? LoaderListState.Ready)?.let { ready ->
                loaderChoice = ready.choices.firstOrNull { it.recommended } ?: ready.choices.firstOrNull()
                refreshAutoName()
            }

            apiMod = api.await()
            performanceState = PerformanceState.Ready(performance.await())
        }
    }

    private suspend fun fetchApiMod(mcVersion: String, loader: InstallLoader): ModVersion? = try {
        when (loader) {
            InstallLoader.FABRIC -> FabricAPIVersions.fetchVersionList(mcVersion)?.firstOrNull()
            InstallLoader.QUILT -> QuiltAPIVersions.fetchVersionList(mcVersion)?.firstOrNull()
            else -> null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private suspend fun fetchLoaderChoices(mcVersion: String, loader: InstallLoader): List<LoaderChoice> =
        when (loader) {
            InstallLoader.FABRIC -> FabricVersions.fetchFabricLoaderList(mcVersion).orEmpty().map {
                LoaderChoice(label = it.version, recommended = it.stable, value = it)
            }
            InstallLoader.QUILT -> QuiltVersions.fetchQuiltLoaderList(mcVersion).orEmpty().map {
                LoaderChoice(label = it.version, recommended = it.stable, value = it)
            }
            InstallLoader.FORGE -> ForgeVersions.fetchForgeList(mcVersion).orEmpty().map {
                LoaderChoice(label = it.versionName, recommended = it.isRecommended, value = it)
            }
            InstallLoader.NEOFORGE -> NeoForgeVersions.fetchNeoForgeList(gameVersion = mcVersion).orEmpty().map {
                LoaderChoice(
                    label = it.versionName,
                    recommended = !it.versionName.contains("beta", ignoreCase = true),
                    value = it
                )
            }
            InstallLoader.VANILLA -> emptyList()
        }

    private fun refreshAutoName() {
        if (nameEditedByUser) return
        val mcVersion = selected?.version?.id ?: return
        val choice = loaderChoice
        name = if (loader == InstallLoader.VANILLA || choice == null) {
            if (loader == InstallLoader.VANILLA) mcVersion else "$mcVersion ${loader.displayName}"
        } else {
            "$mcVersion ${loader.displayName} ${choice.label}"
        }
    }

    /** True when the selection is complete enough to install. */
    val canInstall: Boolean
        get() = selected != null && name.isNotBlank() &&
                (loader == InstallLoader.VANILLA || loaderChoice != null)

    /** The download description handed to the original game installer. */
    fun buildInfo(): GameDownloadInfo? {
        val mcVersion = selected?.version?.id ?: return null
        val choice = loaderChoice?.value
        val extras = if (includePerformance && loader.supportsPerformanceMods) {
            (performanceState as? PerformanceState.Ready)?.mods.orEmpty()
        } else emptyList()
        val api = apiMod.takeIf { includeApi }
        return GameDownloadInfo(
            gameVersion = mcVersion,
            customVersionName = name.trim(),
            forge = choice as? ForgeVersion,
            neoforge = choice as? NeoForgeVersion,
            fabric = choice as? FabricVersion,
            fabricAPI = api.takeIf { loader == InstallLoader.FABRIC },
            quilt = choice as? QuiltVersion,
            quiltAPI = api.takeIf { loader == InstallLoader.QUILT },
            extraMods = extras,
        )
    }
}
