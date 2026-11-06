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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskStage
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightTag
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightPill
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightPillDivider
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightSearchBar
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.ui.theme.MoonlightPalette

private val PanelShape = RoundedCornerShape(22.dp)
private val TileShape = RoundedCornerShape(14.dp)
private val InstalledMint = Color(0xFF7EE0B0)

/**
 * Moonlight version installer (landscape):
 *
 * ```
 * | [Vanilla][Fabric][Forge][NeoForge][Quilt] | [Snapshots]   |  install panel  |
 * | [search version...........................]                |  name, loader   |
 * | [1.21.4] [1.21.1] [1.20.6] [1.20.4] ...   ← version grid   |  build, mods,   |
 * |                                                            |  progress       |
 * ```
 *
 * The panel slides in when a version is picked and shows the install progress
 * (game files, loader, Fabric API, Sodium, Lithium) in place, without a dialog.
 * The download itself is the original Zalith game installer.
 */
@Composable
fun MoonlightInstallScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    installer: GameInstaller?,
    onInstall: (GameDownloadInfo) -> Unit,
    onCancelInstall: () -> Unit,
    onAdvanced: (MinecraftVersion) -> Unit,
) {
    val viewModel: MoonlightInstallViewModel = viewModel(key = "moonlight_install") {
        MoonlightInstallViewModel()
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey),
            Pair(NestedNavKey.DownloadGame::class.java, downloadScreenKey)
        ),
        Triple(NormalNavKey.DownloadGame.SelectGameVersion, downloadGameScreenKey, false)
    ) { isVisible ->
        val ctx = rememberAnimatedContext(isVisible = isVisible)
        val installing = installer != null
        val tasks: List<TitledTask> = installer?.tasksFlow?.collectAsStateWithLifecycle()?.value.orEmpty()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .animated(ctx)
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VersionPicker(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                viewModel = viewModel,
                enabled = !installing
            )

            AnimatedVisibility(
                visible = viewModel.selected != null || installing,
                enter = slideInHorizontally { it / 2 } + fadeIn(),
                exit = slideOutHorizontally { it / 2 } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                    shape = PanelShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    AnimatedContent(
                        targetState = installing,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "installPanel"
                    ) { showProgress ->
                        if (showProgress) {
                            InstallProgress(
                                tasks = tasks,
                                onCancel = onCancelInstall
                            )
                        } else {
                            InstallForm(
                                viewModel = viewModel,
                                onInstall = { viewModel.buildInfo()?.let(onInstall) },
                                onAdvanced = { viewModel.selected?.let(onAdvanced) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionPicker(
    viewModel: MoonlightInstallViewModel,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InstallLoader.entries.forEach { loader ->
                MoonlightPill(
                    label = if (loader == InstallLoader.VANILLA) {
                        stringResource(R.string.moonlight_install_vanilla)
                    } else loader.displayName,
                    selected = viewModel.loader == loader,
                    onClick = { if (enabled) viewModel.changeLoader(loader) }
                )
            }
            MoonlightPillDivider()
            MoonlightPill(
                label = stringResource(R.string.moonlight_install_snapshots),
                selected = viewModel.showSnapshots,
                onClick = { viewModel.changeSnapshots(!viewModel.showSnapshots) }
            )
        }

        MoonlightSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            onSearch = {},
            placeholder = stringResource(R.string.moonlight_install_search)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = viewModel.versionState) {
                is VersionListState.Loading -> CenteredMessage(
                    text = stringResource(R.string.moonlight_install_loading_versions),
                    showProgress = true
                )
                is VersionListState.Failed -> CenteredMessage(
                    modifier = Modifier.clickable { viewModel.loadVersions(force = true) },
                    text = stringResource(R.string.moonlight_install_versions_failed)
                )
                is VersionListState.Ready -> {
                    val versions = remember(state.versions, viewModel.loader, viewModel.showSnapshots, viewModel.query) {
                        viewModel.visibleVersions(state.versions)
                    }
                    val selectedId = viewModel.selected?.version?.id
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(versions, key = { it.version.id }, contentType = { "version" }) { version ->
                            VersionTile(
                                version = version,
                                selected = version.version.id == selectedId,
                                onClick = { if (enabled) viewModel.select(version) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionTile(
    version: MinecraftVersion,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = TileShape,
        color = if (selected) MoonlightPalette.Violet.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MoonlightPalette.Violet else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = version.version.id,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = version.version.releaseTime.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (version.type == MinecraftVersion.Type.Snapshot) {
                    MoonlightTag(text = stringResource(R.string.moonlight_install_snapshots))
                }
            }
        }
    }
}

@Composable
private fun InstallForm(
    viewModel: MoonlightInstallViewModel,
    onInstall: () -> Unit,
    onAdvanced: () -> Unit,
) {
    val version = viewModel.selected ?: return
    val loader = viewModel.loader
    val mcVersion = version.version.id

    val trimmedName = viewModel.name.trim()
    val invalidName = isFilenameInvalid(trimmedName)
    val nameExists = remember(trimmedName) {
        trimmedName.isNotEmpty() && VersionsManager.isVersionExists(trimmedName)
    }
    val nameError = when {
        invalidName != null -> invalidName
        nameExists -> stringResource(R.string.moonlight_install_name_exists)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Minecraft $mcVersion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (loader == InstallLoader.VANILLA) {
                        stringResource(R.string.moonlight_install_vanilla)
                    } else loader.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = { viewModel.clearSelection() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.moonlight_install_close)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                value = viewModel.name,
                onValueChange = { viewModel.editName(it) },
                label = { Text(text = stringResource(R.string.moonlight_install_name)) },
                singleLine = true,
                isError = nameError != null,
                supportingText = if (nameError != null) {
                    { Text(text = nameError) }
                } else null,
                shape = RoundedCornerShape(14.dp)
            )

            if (loader != InstallLoader.VANILLA) {
                LoaderVersionPicker(
                    loaderName = loader.displayName,
                    mcVersion = mcVersion,
                    state = viewModel.loaderState,
                    choice = viewModel.loaderChoice,
                    onChoose = { viewModel.chooseLoader(it) },
                    onRetry = { viewModel.select(version) }
                )
            }

            val performance = viewModel.performanceState
            val stillLoading = performance is PerformanceState.Loading

            if (loader == InstallLoader.FABRIC || loader == InstallLoader.QUILT) {
                val api = viewModel.apiMod
                ToggleRow(
                    title = if (loader == InstallLoader.QUILT) "Quilted Fabric API" else stringResource(R.string.moonlight_install_fabric_api),
                    summary = when {
                        api != null -> "${api.displayName} · " + stringResource(R.string.moonlight_install_fabric_api_summary)
                        stillLoading -> stringResource(R.string.moonlight_install_fabric_api_summary)
                        else -> stringResource(R.string.moonlight_install_performance_unavailable, "Minecraft $mcVersion")
                    },
                    checked = viewModel.includeApi && api != null,
                    enabled = api != null,
                    onToggle = { viewModel.includeApi = it }
                )
            }

            if (loader.supportsPerformanceMods) {
                val mods = (performance as? PerformanceState.Ready)?.mods.orEmpty()
                ToggleRow(
                    title = stringResource(R.string.moonlight_install_performance),
                    summary = when {
                        stillLoading -> stringResource(R.string.moonlight_install_performance_summary)
                        mods.isEmpty() -> stringResource(R.string.moonlight_install_performance_unavailable, "Minecraft $mcVersion")
                        mods.size < PerformanceMods.names.size -> stringResource(
                            R.string.moonlight_install_performance_partial,
                            mods.joinToString { "${it.name} ${it.version.displayName}" }
                        )
                        else -> mods.joinToString(" · ") { "${it.name} ${it.version.displayName}" }
                    },
                    checked = viewModel.includePerformance && mods.isNotEmpty(),
                    enabled = mods.isNotEmpty(),
                    loading = stillLoading,
                    onToggle = { viewModel.includePerformance = it }
                )
            } else if (loader == InstallLoader.FORGE) {
                Text(
                    text = stringResource(R.string.moonlight_install_performance) + ": " +
                            stringResource(R.string.moonlight_install_performance_unavailable, loader.displayName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onAdvanced) {
                Text(text = stringResource(R.string.moonlight_install_advanced), maxLines = 1)
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onInstall,
                enabled = viewModel.canInstall && nameError == null &&
                        // wait for the mods lookup so the chosen extras are really included
                        !(loader.supportsPerformanceMods && viewModel.performanceState is PerformanceState.Loading),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonlightPalette.Violet,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_download_2_filled),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.moonlight_install_button),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoaderVersionPicker(
    loaderName: String,
    mcVersion: String,
    state: LoaderListState,
    choice: LoaderChoice?,
    onChoose: (LoaderChoice) -> Unit,
    onRetry: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when (state) {
                    is LoaderListState.Ready -> expanded = true
                    is LoaderListState.Failed -> onRetry()
                    else -> {}
                }
            },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.moonlight_install_loader_version, loaderName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (state) {
                            is LoaderListState.Loading -> stringResource(R.string.moonlight_install_loader_loading, loaderName)
                            is LoaderListState.Empty -> stringResource(R.string.moonlight_install_loader_none, loaderName, mcVersion)
                            is LoaderListState.Failed -> stringResource(R.string.moonlight_install_loader_failed, loaderName)
                            is LoaderListState.Ready -> choice?.let {
                                if (it.recommended && it == state.choices.firstOrNull { c -> c.recommended }) {
                                    stringResource(R.string.moonlight_install_loader_latest, it.label)
                                } else it.label
                            } ?: "—"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (state is LoaderListState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (state is LoaderListState.Ready) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                        contentDescription = null
                    )
                }
            }
        }

        if (state is LoaderListState.Ready) {
            DropdownMenu(
                modifier = Modifier.heightIn(max = 280.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(14.dp)
            ) {
                state.choices.take(60).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.label,
                                fontWeight = if (item == choice) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingIcon = if (item == choice) {
                            {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null
                                )
                            }
                        } else null,
                        onClick = {
                            onChoose(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    loading: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onToggle
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

/**
 * Install progress inside the panel: overall bar plus one row per step
 * (game files, loader, Fabric API, Sodium, Lithium, final setup).
 */
@Composable
private fun InstallProgress(
    tasks: List<TitledTask>,
    onCancel: () -> Unit,
) {
    val stages = tasks.map { task ->
        key(task) { task.task.stage.collectAsStateWithLifecycle().value }
    }
    val done = stages.count { it == TaskStage.COMPLETED }
    val total = tasks.size.coerceAtLeast(1)
    val overall by animateFloatAsState(done / total.toFloat(), label = "installOverall")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.moonlight_install_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$done/${tasks.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            progress = { overall },
            color = MoonlightPalette.Violet,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task -> TaskRow(task) }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancel,
            shape = RoundedCornerShape(50)
        ) {
            Text(text = stringResource(R.string.moonlight_install_cancel))
        }
    }
}

@Composable
private fun TaskRow(task: TitledTask) {
    val stage by task.task.stage.collectAsStateWithLifecycle()
    val progress by task.task.progress.collectAsStateWithLifecycle()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            when (stage) {
                TaskStage.COMPLETED -> Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = InstalledMint
                )
                TaskStage.RUNNING -> CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MoonlightPalette.Lavender
                )
                TaskStage.PREPARING -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AndroidStringText(
                text = task.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (stage == TaskStage.PREPARING) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (stage == TaskStage.RUNNING) {
                if (progress < 0f) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = MoonlightPalette.Lavender,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        progress = { progress },
                        color = MoonlightPalette.Lavender,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    text: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
