/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content.download.addon.download

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightCoverArt
import com.movtery.zalithlauncher.moonlight.ui.home.MoonlightTag
import com.movtery.zalithlauncher.moonlight.ui.home.moonlightTags
import com.movtery.zalithlauncher.moonlight.ui.project.InstallButtonState
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightDependencyNote
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightInstallButton
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightOption
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightPanelHeading
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightSelectField
import com.movtery.zalithlauncher.moonlight.ui.project.MoonlightTabs
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import java.io.File
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.addon.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.addon.platform.cacheKey
import com.movtery.zalithlauncher.game.download.addon.platform.getProject
import com.movtery.zalithlauncher.game.download.addon.platform.getVersionById
import com.movtery.zalithlauncher.game.download.addon.platform.getVersions
import com.movtery.zalithlauncher.game.download.addon.platform.isAllNull
import com.movtery.zalithlauncher.game.download.addon.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.download.addon.utils.getTranslations
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.versioninfo.filterRelease
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.AddonIcon
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.AddonVersionItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.ClassesIdentifier
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.DependencyEntry
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.DownloadAddonState
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.DownloadAddonVersionLoading
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.FavoriteIdentifier
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.ProjectUrlsContent
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.ScreenshotItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.VersionInfoMap
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.initAll
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.mapWithVersions
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jackhuang.hmcl.addon.AddonProjectInfo
import kotlin.time.Duration.Companion.milliseconds

private class DownloadScreenViewModel(
    private val platform: Platform,
    private val projectId: String,
    initialClasses: PlatformClasses
): ViewModel() {
    /**
     * 资源类型，优先使用项目详情返回的准确类型
     */
    var classes by mutableStateOf(initialClasses)
        private set

    //版本
    private var _versionsList by mutableStateOf<List<VersionInfoMap>>(emptyList())
    //未经映射的原始版本数据，类型修正后用于重新映射
    //Moonlight: also read by the install panel to pick a compatible version
    var rawVersions by mutableStateOf<List<PlatformVersion>>(emptyList())
        private set
    var versionsResult by mutableStateOf<DownloadAddonState<List<VersionInfoMap>>>(DownloadAddonState.Getting())
    var versionsLoading by mutableStateOf<DownloadAddonVersionLoading>(DownloadAddonVersionLoading.None)
        private set

    var showOnlyMCRelease by mutableStateOf(true)
    var searchMCVersion by mutableStateOf("")

    fun filterWith(
        showOnlyMCRelease: Boolean = this.showOnlyMCRelease,
        searchMCVersion: String = this.searchMCVersion
    ) {
        this.showOnlyMCRelease = showOnlyMCRelease
        this.searchMCVersion = searchMCVersion
        viewModelScope.launch {
            versionsLoading = DownloadAddonVersionLoading.None
            val infos = _versionsList.filterInfos()
            versionsResult = DownloadAddonState.Success(infos)
        }
    }

    private fun List<VersionInfoMap>.filterInfos(): List<VersionInfoMap> {
        return filter { info ->
            (!showOnlyMCRelease || filterRelease(info.gameVersion)) &&
                    (searchMCVersion.isEmpty() || info.gameVersion.contains(searchMCVersion, true))
        }
    }

    /**
     * 类型修正后，使用原始版本数据重新映射版本列表
     */
    private fun remapVersions() {
        if (rawVersions.isEmpty()) return
        _versionsList = rawVersions.mapWithVersions(classes)
        versionsResult = DownloadAddonState.Success(_versionsList.filterInfos())
    }

    fun getVersions() {
        viewModelScope.launch {
            versionsResult = DownloadAddonState.Getting()
            //重新加载时重试此前获取失败的依赖项目
            failedDependencyProjects.clear()
            if (platform == Platform.CURSEFORGE) {
                versionsLoading = DownloadAddonVersionLoading.StartLoadPage
            }

            getVersions(
                projectID = projectId,
                platform = platform,
                pageCallback = { chunk, page ->
                    versionsLoading = DownloadAddonVersionLoading.LoadingPage(chunk, page)
                },
                onSuccess = { result ->
                    val versions: List<PlatformVersion> = result.initAll(projectId)
                    rawVersions = versions

                    //版本列表先行展示，依赖项目信息改为后台缓存，不再阻塞列表加载
                    _versionsList = versions.mapWithVersions(classes)
                    versionsResult = DownloadAddonState.Success(_versionsList.filterInfos())
                    versionsLoading = DownloadAddonVersionLoading.None

                    if (classes == PlatformClasses.MOD_PACK) return@getVersions
                    val dependencies = versions
                        .flatMap { it.platformDependencies() }
                        .distinctBy { it.cacheKey() }
                    if (dependencies.isEmpty()) return@getVersions

                    viewModelScope.launch {
                        val semaphore = Semaphore(8)
                        dependencies.map { dependency ->
                            async {
                                semaphore.withPermit {
                                    cacheDependencyProject(
                                        platform = dependency.platform,
                                        dependency = dependency
                                    )
                                }
                            }
                        }.awaitAll()
                    }
                },
                onError = {
                    versionsResult = it
                    versionsLoading = DownloadAddonVersionLoading.None
                }
            )
        }
    }

    //项目信息
    var projectResult by mutableStateOf<DownloadAddonState<Pair<PlatformProject, AddonProjectInfo?>>>(DownloadAddonState.Getting())

    fun getProject() {
        viewModelScope.launch {
            projectResult = DownloadAddonState.Getting()
            getProject(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    //以项目详情返回的类型为准
                    val accurateClasses = result.platformClasses(classes)
                    if (accurateClasses != classes) {
                        classes = accurateClasses
                        remapVersions()
                    }
                    val mcmod = classes.getTranslations()?.getInfoByCurseForgeId(result.platformSlug())
                    projectResult = DownloadAddonState.Success(Pair(result, mcmod))
                },
                onError = { state, _ ->
                    projectResult = state
                }
            )
        }
    }

    //缓存依赖项目
    val cachedDependencyProject = mutableStateMapOf<String, PlatformProject>()
    //该依赖项目未找到，但是多个版本同时依赖这个不存在的项目
    //就会进行很多次无效的访问，非常耗时
    //需要记录不存在的依赖项目的id，避免下次继续获取
    val notFoundDependencyProjects = mutableStateListOf<String>()
    //依赖项目信息获取失败，在对话框里展示占位项
    val failedDependencyProjects = mutableStateListOf<String>()

    /**
     * 缓存依赖项目
     */
    private suspend fun cacheDependencyProject(
        platform: Platform,
        dependency: PlatformVersion.PlatformDependency
    ) {
        val key = dependency.cacheKey()
        if (notFoundDependencyProjects.contains(key) || cachedDependencyProject.containsKey(key)) return

        try {
            val projectId = dependency.projectId ?: run {
                //依赖只标注了精确版本，先通过版本反查其所属项目
                getVersionById(
                    versionId = dependency.versionId
                        ?: error("The dependency does not provide a project id or a version id."),
                    platform = platform,
                    printLog = false
                ).platformProjectId()
            }
            getProject<PlatformProject>(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    cachedDependencyProject[key] = result
                },
                onError = { _, e ->
                    if (e.isNotFound()) {
                        notFoundDependencyProjects.add(key)
                    } else {
                        if (!failedDependencyProjects.contains(key)) failedDependencyProjects.add(key)
                    }
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (e.isNotFound()) {
                notFoundDependencyProjects.add(key)
            } else {
                if (!failedDependencyProjects.contains(key)) failedDependencyProjects.add(key)
            }
        }
    }

    init {
        //初始化后，获取项目、版本信息
        getVersions()
        getProject()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * 平台的接口是否返回了未找到
 */
private fun Throwable.isNotFound(): Boolean =
    this is ClientRequestException && response.status.value == 404

@Composable
private fun rememberDownloadAddonViewModel(
    key: NormalNavKey.DownloadAddon
): DownloadScreenViewModel {
    return viewModel(
        key = key.toString()
    ) {
        DownloadScreenViewModel(
            platform = key.platform,
            projectId = key.projectId,
            initialClasses = key.classes
        )
    }
}

/**
 * Moonlight project page:
 *
 * ```
 * | banner: icon, title, author · downloads    |  INSTALL                    |
 * | Description · Gallery · Versions · Deps    |  Install in  [instance  v]  |
 * | tab content                                |  Version     [auto      v]  |
 * |                                            |  + Includes Sodium (dep.)   |
 * |                                            |  [ Downloading… 42%      ]  |
 * ```
 *
 * The panel picks the newest version compatible with the selected instance
 * (same Minecraft version and loader) and installs it with its required
 * dependencies in one tap; the Versions tab keeps the full list and the
 * original download dialog.
 *
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param currentKey 当前的Key
 * @param installedChecker 查询版本本地是否已安装，null 则不进行已安装标注
 * @param onQuickInstall one-tap install into the selected instance; null opens the download dialog
 */
@Composable
fun DownloadAddonScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    currentKey: TitledNavKey?,
    key: NormalNavKey.DownloadAddon,
    eventViewModel: EventViewModel,
    onItemClicked: (PlatformClasses, PlatformVersion, iconUrl: String?, deps: List<DependencyEntry>) -> Unit,
    nestedNavKeyClass: Class<out TitledNavKey>? = null,
    @Suppress("UNUSED_PARAMETER") versionsUIWeight: Float = 6.5f,
    @Suppress("UNUSED_PARAMETER") projectUIWeight: Float = 3.5f,
    installedChecker: ((PlatformVersion) -> InstalledMod?)? = null,
    onQuickInstall: ((PlatformClasses, PlatformVersion, List<DependencyEntry>) -> Unit)? = null,
) {
    val viewModel: DownloadScreenViewModel = rememberDownloadAddonViewModel(key)

    BaseScreen(
        levels1 = listOf(
            Pair(nestedNavKeyClass ?: NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(key, currentKey, false),
    ) { isVisible ->
        val contentCtx = rememberAnimatedContext(isVisible = isVisible)
        val panelCtx = rememberAnimatedContext(isVisible = isVisible, delayMillis = 60)
        var tab by rememberSaveable { mutableIntStateOf(0) }

        val classes = viewModel.classes
        val isModpack = classes == PlatformClasses.MOD_PACK
        val currentInstance by VersionsManager.currentVersion.collectAsStateWithLifecycle()

        val dependenciesOf: (PlatformVersion) -> List<DependencyEntry> = { version ->
            version.platformDependencies().mapNotNull { dep ->
                val depKey = dep.cacheKey()
                val project = viewModel.cachedDependencyProject[depKey]
                when {
                    project != null -> DependencyEntry(dep, project)
                    viewModel.notFoundDependencyProjects.contains(depKey) ->
                        DependencyEntry(dep, null, notFound = true)
                    viewModel.failedDependencyProjects.contains(depKey) ->
                        DependencyEntry(dep, null)
                    //依赖项目信息仍在获取中
                    else -> null
                }
            }
        }

        // Versions that fit the selected instance, newest first
        val compatible = remember(viewModel.rawVersions, currentInstance, classes) {
            viewModel.rawVersions
                .filter { isModpack || it.fitsInstance(currentInstance, classes) }
                .sortedByDescending { it.platformDatePublished() }
        }
        val autoVersion = compatible.firstOrNull { it.platformReleaseType() == PlatformReleaseType.RELEASE }
            ?: compatible.firstOrNull()
        var pickedVersionId by rememberSaveable(currentInstance?.getVersionName()) { mutableStateOf<String?>(null) }
        val chosen = compatible.firstOrNull { it.platformId() == pickedVersionId } ?: autoVersion

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .animated(contentCtx),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProjectBanner(
                    projectResult = viewModel.projectResult,
                    projectId = key.projectId,
                    platform = key.platform,
                    classes = classes,
                    fallbackIconUrl = key.iconUrl,
                    onReload = { viewModel.getProject() }
                )
                MoonlightTabs(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = listOf(
                        stringResource(R.string.moonlight_project_tab_description),
                        stringResource(R.string.moonlight_project_tab_gallery),
                        stringResource(R.string.moonlight_project_tab_versions),
                        stringResource(R.string.moonlight_project_tab_dependencies)
                    ),
                    selected = tab,
                    onSelect = { tab = it }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (tab) {
                        0 -> DescriptionTab(
                            projectResult = viewModel.projectResult,
                            classes = classes,
                            openLink = { url -> eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url)) }
                        )
                        1 -> GalleryTab(projectResult = viewModel.projectResult)
                        2 -> Versions(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            installedChecker = installedChecker,
                            onReload = { viewModel.getVersions() },
                            onItemClicked = { version ->
                                onItemClicked(classes, version, key.iconUrl, dependenciesOf(version))
                            }
                        )
                        else -> DependenciesTab(
                            version = chosen,
                            entries = chosen?.let(dependenciesOf).orEmpty()
                        )
                    }
                }
            }

            InstallPanel(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .animated(panelCtx, from = AnimatedDirection.Right),
                classes = classes,
                versionsReady = viewModel.versionsResult is DownloadAddonState.Success,
                currentInstance = currentInstance,
                compatible = compatible,
                chosen = chosen,
                pickedVersionId = pickedVersionId,
                onPickVersion = { pickedVersionId = it },
                dependencyNames = chosen?.let(dependenciesOf).orEmpty()
                    .filter { it.dependency.type == PlatformDependencyType.REQUIRED }
                    .mapNotNull { it.project?.platformTitle() },
                installedChecker = installedChecker,
                onInstall = {
                    chosen?.let { version ->
                        val deps = dependenciesOf(version)
                        if (onQuickInstall != null && !isModpack) {
                            onQuickInstall(classes, version, deps)
                        } else {
                            onItemClicked(classes, version, key.iconUrl, deps)
                        }
                    }
                }
            )
        }
    }
}

/**
 * Whether a project version runs on [instance]: same Minecraft version and, for
 * mods, one of the instance loaders (any loader for resource packs, shaders, worlds).
 */
private fun PlatformVersion.fitsInstance(instance: Version?, classes: PlatformClasses): Boolean {
    val info = instance?.getVersionInfo() ?: return false
    if (!platformGameVersion().contains(info.minecraftVersion)) return false
    val loaders = platformLoaders()
    if (classes != PlatformClasses.MOD || loaders.isEmpty()) return true
    val instanceLoaders = info.loaderInfos.map { it.loader.displayName }
    return loaders.any { label -> instanceLoaders.any { it.equals(label.getDisplayName(), ignoreCase = true) } }
}

private val BannerText = Color(0xFFE8E2F4)

@Composable
private fun ProjectBanner(
    projectResult: DownloadAddonState<Pair<PlatformProject, AddonProjectInfo?>>,
    projectId: String,
    platform: Platform,
    classes: PlatformClasses,
    fallbackIconUrl: String?,
    onReload: () -> Unit,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(shape)
    ) {
        MoonlightCoverArt(modifier = Modifier.fillMaxSize(), seed = projectId)

        when (projectResult) {
            is DownloadAddonState.Getting -> ShimmerBox(modifier = Modifier.fillMaxSize())
            is DownloadAddonState.Error -> {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = {
                        AndroidStringText(
                            text = androidText(R.string.download_addon_failed_to_get_project, projectResult.message)
                        )
                    },
                    onClick = onReload
                )
            }
            is DownloadAddonState.Success -> {
                val (project, mcmod) = projectResult.result
                val banner = remember(project) { project.platformScreenshots().firstOrNull()?.imageUrl }
                val title = remember(project, mcmod) { mcmod.getMcmodTitle(project.platformTitle(), context) }
                val authors = remember(project) { project.platformAuthors().orEmpty() }
                val downloads = remember(project) { formatNumberByLocale(context, project.platformDownloadCount()) }

                if (banner != null) {
                    // One image per page, sized to the banner and cached by the app's image loader
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = banner,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0x1A0B0814),
                                1f to Color(0xE60B0814)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AddonIcon(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, Color(0xFF0B0814), RoundedCornerShape(14.dp)),
                        size = 56.dp,
                        iconUrl = project.platformIconUrl() ?: fallbackIconUrl
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BannerText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(
                                authors.takeIf { it.isNotEmpty() }?.let {
                                    stringResource(R.string.moonlight_store_by, it.joinToString(", "))
                                },
                                stringResource(R.string.moonlight_project_downloads, downloads)
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = BannerText.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 资源类型、收藏开关
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClassesIdentifier(
                classes = classes,
                iconSize = 14.dp,
                textStyle = MaterialTheme.typography.labelSmall
            )
            val isFavorite = FavoriteProjectsRepository.isFavorite(platform, projectId)
            FavoriteIdentifier(
                isFavorite = isFavorite,
                iconSize = 14.dp,
                textStyle = MaterialTheme.typography.labelSmall,
                onClick = {
                    if (isFavorite) {
                        FavoriteProjectsRepository.unfavorite(platform, projectId)
                    } else {
                        //项目数据未就绪时无法生成收藏缓存，忽略此次操作
                        (projectResult as? DownloadAddonState.Success)
                            ?.result?.first?.let { project ->
                                FavoriteProjectsRepository.favorite(project, classes)
                            }
                    }
                }
            )
        }
    }
}

@Composable
private fun DescriptionTab(
    projectResult: DownloadAddonState<Pair<PlatformProject, AddonProjectInfo?>>,
    classes: PlatformClasses,
    openLink: (String) -> Unit,
) {
    val project = (projectResult as? DownloadAddonState.Success)?.result ?: run {
        if (projectResult is DownloadAddonState.Getting) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(Modifier.fillMaxWidth(0.9f).height(14.dp).clip(RoundedCornerShape(6.dp)))
                ShimmerBox(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(6.dp)))
            }
        }
        return
    }
    val (info, mcmod) = project
    val summary = remember(info) { info.platformSummary() }
    val urls = remember(info, classes) { info.platformUrls(classes) }
    val loaders = remember(info) { info.platformModLoaders().orEmpty().map { it.getDisplayName() } }
    val categories = info.platformCategories(classes).orEmpty()
        .sortedBy { it.index() }
        .map { stringResource(it.getDisplayName()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (categories.isNotEmpty() || loaders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (categories + loaders).forEach { MoonlightTag(text = it) }
                }
            }
        }
        if (!urls.isAllNull()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.moonlight_project_full_description),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProjectUrlsContent(
                        platform = info.platform(),
                        urls = urls,
                        mcmod = mcmod,
                        openLink = openLink,
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryTab(
    projectResult: DownloadAddonState<Pair<PlatformProject, AddonProjectInfo?>>,
) {
    val project = (projectResult as? DownloadAddonState.Success)?.result?.first
    val screenshots = remember(project) { project?.platformScreenshots().orEmpty() }
    if (project != null && screenshots.isEmpty()) {
        PageMessage(text = stringResource(R.string.moonlight_project_no_gallery))
        return
    }
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 220.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        gridItems(screenshots) { screenshot ->
            ScreenshotItemLayout(
                modifier = Modifier.fillMaxWidth(),
                screenshot = screenshot,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun DependenciesTab(
    version: PlatformVersion?,
    entries: List<DependencyEntry>,
) {
    val all = version?.platformDependencies().orEmpty()
        .filter { it.type == PlatformDependencyType.REQUIRED || it.type == PlatformDependencyType.OPTIONAL }
    if (version != null && all.isEmpty()) {
        PageMessage(text = stringResource(R.string.moonlight_project_no_dependencies))
        return
    }
    val shown = entries.filter {
        it.dependency.type == PlatformDependencyType.REQUIRED || it.dependency.type == PlatformDependencyType.OPTIONAL
    }.sortedBy { if (it.dependency.type == PlatformDependencyType.REQUIRED) 0 else 1 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shown) { entry ->
            val required = entry.dependency.type == PlatformDependencyType.REQUIRED
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AddonIcon(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                    size = 40.dp,
                    iconUrl = entry.project?.platformIconUrl()
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = entry.project?.platformTitle() ?: entry.dependency.projectId ?: "?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                MoonlightTag(
                    text = stringResource(
                        if (required) R.string.moonlight_project_required else R.string.moonlight_project_optional
                    )
                )
            }
        }
        if (shown.size < all.size) {
            item {
                Text(
                    text = stringResource(R.string.moonlight_project_dependencies_loading),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PageMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InstallPanel(
    classes: PlatformClasses,
    versionsReady: Boolean,
    currentInstance: Version?,
    compatible: List<PlatformVersion>,
    chosen: PlatformVersion?,
    pickedVersionId: String?,
    onPickVersion: (String?) -> Unit,
    dependencyNames: List<String>,
    installedChecker: ((PlatformVersion) -> InstalledMod?)?,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isModpack = classes == PlatformClasses.MOD_PACK
    val instances by VersionsManager.versions.collectAsStateWithLifecycle()
    val validInstances = remember(instances) { instances.filter { it.isValid() } }

    // The download task of the chosen file (same id the download code gives it)
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    val fileKey = chosen?.let { it.platformSha1() ?: it.platformFileName() }
    val runningTask = fileKey?.let { k -> tasks.firstOrNull { it.id == k || it.id.startsWith("$k|") } }
    val taskProgress = runningTask?.progress?.collectAsStateWithLifecycle()?.value ?: 0f

    val fileInInstance = remember(chosen, currentInstance, runningTask == null) {
        val instance = currentInstance
        if (chosen == null || instance == null || isModpack || classes == PlatformClasses.SAVES) {
            false
        } else {
            File(File(instance.getGameDir(), classes.versionFolder.folderName), chosen.platformFileName()).exists()
        }
    }

    val buttonState = when {
        !versionsReady -> InstallButtonState.Loading
        runningTask != null -> InstallButtonState.Downloading(taskProgress)
        chosen == null -> InstallButtonState.Unavailable
        isModpack -> InstallButtonState.Ready
        currentInstance == null -> InstallButtonState.Unavailable
        fileInInstance || installedChecker?.invoke(chosen) != null -> InstallButtonState.Installed
        else -> InstallButtonState.Ready
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MoonlightPanelHeading(text = stringResource(R.string.moonlight_project_install))

                if (!isModpack) {
                    val options = validInstances.map { instance ->
                        MoonlightOption(
                            value = instance.getVersionName(),
                            label = (listOf(instance.getVersionName()) + instance.moonlightTags()).joinToString(" · ")
                        )
                    }
                    MoonlightSelectField(
                        label = stringResource(R.string.moonlight_project_install_in),
                        options = options,
                        selected = options.firstOrNull { it.value == currentInstance?.getVersionName() },
                        onSelect = { option ->
                            validInstances.firstOrNull { it.getVersionName() == option.value }
                                ?.let { VersionsManager.saveVersion(it) }
                        },
                        placeholder = stringResource(R.string.moonlight_project_no_instance)
                    )
                    if (validInstances.size > 1) {
                        Text(
                            text = stringResource(R.string.moonlight_project_selected_instance),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val autoOption = MoonlightOption<String?>(
                    value = null,
                    label = stringResource(R.string.moonlight_project_version_auto)
                )
                val versionOptions = listOf(autoOption) + compatible.take(50).map { version ->
                    MoonlightOption<String?>(value = version.platformId(), label = version.platformDisplayName())
                }
                MoonlightSelectField(
                    label = stringResource(R.string.moonlight_project_version),
                    options = if (compatible.isEmpty()) emptyList() else versionOptions,
                    selected = versionOptions.firstOrNull { it.value == pickedVersionId } ?: autoOption,
                    onSelect = { onPickVersion(it.value) },
                    placeholder = stringResource(R.string.moonlight_project_loading_versions)
                )
                if (chosen != null) {
                    Text(
                        text = chosen.platformDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                dependencyNames.forEach { name ->
                    MoonlightDependencyNote(text = stringResource(R.string.moonlight_project_includes_dependency, name))
                }

                if (versionsReady && compatible.isEmpty() && !isModpack && currentInstance != null) {
                    Text(
                        text = stringResource(R.string.moonlight_project_no_compatible, currentInstance.getVersionName()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            MoonlightInstallButton(
                modifier = Modifier.fillMaxWidth(),
                state = buttonState,
                readyLabel = stringResource(
                    if (isModpack) R.string.moonlight_project_install_modpack else R.string.moonlight_project_install
                ),
                loadingLabel = stringResource(R.string.moonlight_project_loading_versions),
                installedLabel = stringResource(R.string.moonlight_project_installed),
                downloadingLabel = { percent -> context.getString(R.string.moonlight_project_downloading, percent) },
                installingLabel = stringResource(R.string.moonlight_project_installing),
                onClick = onInstall
            )
        }
    }
}

/**
 * 所有版本列表
 */
@Composable
private fun Versions(
    modifier: Modifier = Modifier,
    viewModel: DownloadScreenViewModel,
    installedChecker: ((PlatformVersion) -> InstalledMod?)? = null,
    onReload: () -> Unit = {},
    onItemClicked: (PlatformVersion) -> Unit = {}
) {
    when (val versions = viewModel.versionsResult) {
        is DownloadAddonState.Getting -> {
            Box(
                modifier.padding(all = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.animateContentSize()
                    ) {
                        when (val state = viewModel.versionsLoading) {
                            is DownloadAddonVersionLoading.None -> {}
                            is DownloadAddonVersionLoading.StartLoadPage -> {
                                Text(
                                    text = stringResource(R.string.download_addon_loading_page_data),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is DownloadAddonVersionLoading.LoadingPage -> {
                                Text(
                                    text = stringResource(R.string.download_addon_loaded_chunk_page, state.chunk, state.page),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    LinearWavyProgressIndicator(
                        modifier = Modifier.width(168.dp),
                        wavelength = 32.dp
                    )
                }
            }
        }
        is DownloadAddonState.Success -> {
            Column(modifier = modifier) {
                //简单过滤条件
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CheckChip(
                        selected = viewModel.showOnlyMCRelease,
                        onClick = {
                            viewModel.filterWith(showOnlyMCRelease = viewModel.showOnlyMCRelease.not())
                        },
                        label = {
                            Text(text = stringResource(R.string.download_addon_show_only_mc_release))
                        },
                    )

                    SimpleTextInputField(
                        modifier = Modifier.weight(1f),
                        value = viewModel.searchMCVersion,
                        onValueChange = { viewModel.filterWith(searchMCVersion = it) },
                        singleLine = true,
                        textStyle = TextStyle(color = onCardColor()).copy(fontSize = 12.sp),
                        hint = {
                            Text(
                                text = stringResource(R.string.download_addon_search_mc_versions),
                                style = TextStyle(color = onCardColor()).copy(fontSize = 12.sp)
                            )
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )

                val scrollState = rememberLazyListState()

                LaunchedEffect(Unit) {
                    delay(100L.milliseconds)
                    runCatching {
                        val result = versions.result
                        val index = versions.result.indexOfFirst { it.isAdapt }
                        if (index >= 0 && index < result.size) {
                            //自动滚动到适配的资源版本
                            scrollState.animateScrollToItem(index)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    state = scrollState
                ) {
                    items(
                        items = versions.result,
                        key = { "${it.gameVersion}_${it.loader?.getDisplayName()}" }
                    ) { info ->
                        AddonVersionItemLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            infoMap = info,
                            installedChecker = installedChecker,
                            onItemClicked = onItemClicked
                        )
                    }
                }
            }
        }
        is DownloadAddonState.Error -> {
            Box(modifier.padding(all = 12.dp)) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = {
                        AndroidStringText(
                            text = androidText(
                                R.string.download_addon_failed_to_get_versions,
                                versions.message
                            )
                        )
                    },
                    onClick = onReload
                )
            }
        }
    }
}

/**
 * 项目信息板块
 */
@Composable
private fun ProjectInfo(
    modifier: Modifier = Modifier,
    projectResult: DownloadAddonState<Pair<PlatformProject, AddonProjectInfo?>>,
    platform: Platform,
    projectId: String,
    classes: PlatformClasses,
    onReload: () -> Unit = {},
    openLink: (url: String) -> Unit = {}
) {
    val context = LocalContext.current
    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier) {
            when (projectResult) {
                is DownloadAddonState.Getting -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //图标、标题、简介的骨架
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ShimmerBox(
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .size(72.dp)
                                )
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    //标题
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    //简介
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
                is DownloadAddonState.Success -> {
                    val (project, mcmod) = projectResult.result
                    //项目基本信息
                    val platform = remember { project.platform() }
                    val iconUrl = remember { project.platformIconUrl() }
                    val title = remember { project.platformTitle() }
                    val summary = remember { project.platformSummary() }
                    val urls = remember(classes) { project.platformUrls(classes) }
                    val screenshots = remember { project.platformScreenshots() }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //图标、标题、简介
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AddonIcon(
                                    modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                                    size = 72.dp,
                                    iconUrl = iconUrl
                                )
                                //标题、简介
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = mcmod.getMcmodTitle(title, context),
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        //相关链接
                        if (!urls.isAllNull()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.download_addon_links),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    ProjectUrlsContent(
                                        platform = platform,
                                        urls = urls,
                                        mcmod = mcmod,
                                        openLink = openLink,
                                    )
                                }
                            }
                        }

                        //屏幕截图
                        items(screenshots) { screenshot ->
                            ScreenshotItemLayout(
                                modifier = Modifier.fillMaxWidth(),
                                screenshot = screenshot
                            )
                        }
                    }
                }
                is DownloadAddonState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = 12.dp)
                    ) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = {
                                AndroidStringText(
                                    text = androidText(
                                        R.string.download_addon_failed_to_get_project,
                                        projectResult.message
                                    )
                                )
                            },
                            onClick = onReload
                        )
                    }
                }
            }

            // 资源类型、收藏开关
            Row(
                modifier = Modifier.padding(all = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClassesIdentifier(
                    classes = classes,
                    iconSize = 16.dp,
                    textStyle = MaterialTheme.typography.labelMedium
                )

                val isFavorite = FavoriteProjectsRepository.isFavorite(platform, projectId)
                FavoriteIdentifier(
                    isFavorite = isFavorite,
                    iconSize = 16.dp,
                    textStyle = MaterialTheme.typography.labelMedium,
                    onClick = {
                        if (isFavorite) {
                            FavoriteProjectsRepository.unfavorite(platform, projectId)
                        } else {
                            //项目数据未就绪时无法生成收藏缓存，忽略此次操作
                            (projectResult as? DownloadAddonState.Success)
                                ?.result?.first?.let { project ->
                                    FavoriteProjectsRepository.favorite(project, classes)
                                }
                        }
                    }
                )
            }
        }
    }
}