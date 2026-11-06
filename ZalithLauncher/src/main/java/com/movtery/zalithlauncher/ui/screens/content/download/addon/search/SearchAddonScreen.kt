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

package com.movtery.zalithlauncher.ui.screens.content.download.addon.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.addon.utils.getMcmodTitle
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightFilterButton
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightFilterSheet
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightPill
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightPillDivider
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightPlatformToggle
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightProjectCard
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightProjectCardSkeleton
import com.movtery.zalithlauncher.moonlight.ui.store.MoonlightSearchBar
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.PageController
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.addon.platform.navigatePage
import com.movtery.zalithlauncher.game.download.addon.platform.nextPage
import com.movtery.zalithlauncher.game.download.addon.platform.previousPage
import com.movtery.zalithlauncher.game.download.addon.platform.searchAddons
import com.movtery.zalithlauncher.game.download.addon.utils.searchMcMods
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.popularVersions
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.AddonPage
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.SearchAddonState
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.SearchFilter
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jackhuang.hmcl.addon.AddonProjectInfo

private const val TAG = "SearchAddonScreen"

/**
 * 资源搜索屏幕的 view model
 * @param initialPlatform 初始设定的平台
 * @param platformClasses 资源搜索的类型
 */
private class SearchScreenViewModel(
    initialPlatform: Platform,
    private val platformClasses: PlatformClasses
): ViewModel() {
    var searchResult by mutableStateOf<SearchAddonState>(SearchAddonState.Searching)
    val pages = mutableStateListOf<AddonPage?>()

    var searchPlatform by mutableStateOf(initialPlatform)
    var searchFilter by mutableStateOf(PlatformSearchFilter())

    private val _searchedMcMods = MutableStateFlow<List<AddonProjectInfo>>(emptyList())
    /** 搜索得到的所有 MCMOD 项目 */
    val searchedMcMods = _searchedMcMods.asStateFlow()
    private val _searchedVersions = MutableStateFlow<List<String>>(emptyList())
    /** 搜索得到的所有Minecraft版本 */
    val searchedVersions = _searchedVersions.asStateFlow()

    var currentSearchJob: Job? = null
    var currentSearchMCMODSJob: Job? = null
    var currentSearchVersionJob: Job? = null

    /**
     * 仅更新搜索名称
     */
    fun updateNameFilter(searchName: String) {
        searchFilter = searchFilter.copy(searchName = searchName)
        currentSearchMCMODSJob?.cancel()
        currentSearchMCMODSJob = viewModelScope.launch {
            val result = try {
                searchName.searchMcMods(classes = platformClasses) ?: emptyList()
            } catch (_: CancellationException) {
                emptyList()
            }.take(20) //仅展示20个搜索结果
            withContext(Dispatchers.Main) {
                _searchedMcMods.update { result }
            }
            currentSearchMCMODSJob = null
        }
    }

    /**
     * 仅更新版本名称
     */
    fun updateVersionFilter(version: String) {
        searchFilter = searchFilter.copy(gameVersion = version)
        refreshVerSuggestions(version)
    }

    private fun refreshVerSuggestions(
        version: String
    ) {
        currentSearchVersionJob?.cancel()
        currentSearchVersionJob = viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            val result: List<String> = when {
                version.isEmpty() -> popularVersions
                allVersions.isEmpty() -> popularVersions.filter { ver ->
                    ver.contains(version)
                }.take(20) //仅展示20个搜索结果
                else -> allVersions.filter {
                    it.version.id.contains(version) &&
                            //CurseForge只能使用正式版进行过滤
                            (searchPlatform != Platform.CURSEFORGE || it.type == MinecraftVersion.Type.Release)
                }.map { it.version.id }.take(20) //仅展示20个搜索结果
            }
            withContext(Dispatchers.Main) {
                _searchedVersions.update { result }
            }
            currentSearchVersionJob = null
        }
    }

    /**
     * 重置并重新搜索
     */
    fun resetSearch() {
        pages.clear()
        searchFilter = searchFilter.copy(index = 0) //重置索引到起始处
        search()
    }

    /**
     * 更新过滤器时，重置已有结果，重新触发搜索
     */
    fun researchWithFilter(filter: PlatformSearchFilter) {
        pages.clear()
        searchFilter = filter.copy(index = 0) //重置索引到起始处
        search()
    }

    private fun putResult(result: PlatformSearchResult) {
        result.getAddonPage(platformClasses).also { page ->
            Logger.info(TAG, "Searched page info: {pageNumber: ${page.pageNumber}, pageIndex: ${page.pageIndex}, totalPage: ${page.totalPage}, isLastPage: ${page.isLastPage}}")

            val targetIndex = page.pageNumber - 1

            if (pages.size > targetIndex) {
                pages[targetIndex] = page //替换已有页
            } else {
                while (pages.size < targetIndex) {
                    pages += null
                }
                pages += page
            }

            searchResult = SearchAddonState.Success(page)
        }
    }

    fun search() {
        currentSearchJob?.cancel() //取消上一个搜索

        currentSearchJob = viewModelScope.launch {
            searchResult = SearchAddonState.Searching
            searchAddons(
                searchPlatform = searchPlatform,
                searchFilter = searchFilter,
                platformClasses = platformClasses,
                onSuccess = { result ->
                    putResult(result)
                },
                onError = {
                    searchResult = it
                }
            )
        }
    }

    init {
        //初始化后，执行一次搜索
        search()
        refreshVerSuggestions("")
        viewModelScope.launch {
            runCatching {
                MinecraftVersions.refreshVersions(force = false)
            }.onFailure {
                Logger.warning(TAG, "Failed to refresh Minecraft versions")
            }
        }
    }

    override fun onCleared() {
        currentSearchJob?.cancel()
        currentSearchMCMODSJob?.cancel()
    }
}

@Composable
private fun rememberSearchAddonViewModel(
    navKey: TitledNavKey,
    initialPlatform: Platform,
    platformClasses: PlatformClasses
): SearchScreenViewModel {
    val screenKey = navKey.toString()
    return viewModel(
        key = "${screenKey}_search"
    ) {
        SearchScreenViewModel(initialPlatform, platformClasses)
    }
}

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param screenKey 屏幕的Key
 * @param currentKey 当前的Key
 * @param platformClasses 搜索资源的分类
 * @param initialPlatform 初始搜索平台
 * @param onPlatformChange 搜索平台变更
 * @param enablePlatform 是否允许更改平台
 * @param getCategories 根据平台获取可用的资源类别过滤器
 * @param enableModLoader 是否允许更改模组加载器
 * @param getModloaders 根据平台获取可用的模组加载器过滤器
 * @param mapCategories 通过平台获取类别本地化信息
 * @param swapToDownload 跳转到下载详情页
 * @param installedInfo 查询项目本地是否已安装，键为平台与平台项目ID
 * @param extraFilter 额外的过滤器UI
 */
@Composable
fun SearchAddonScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    platformClasses: PlatformClasses,
    initialPlatform: Platform,
    onPlatformChange: (Platform) -> Unit = {},
    enablePlatform: Boolean = true,
    getCategories: (Platform) -> List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    getModloaders: (Platform) -> List<PlatformDisplayLabel> = { emptyList() },
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null,
    extraFilter: (LazyListScope.() -> Unit)? = null
) {
    val viewModel: SearchScreenViewModel = rememberSearchAddonViewModel(
        navKey = screenKey,
        initialPlatform = initialPlatform,
        platformClasses = platformClasses
    )

    //跟随平台自动变更的内容
    val categories = remember(viewModel.searchPlatform) {
        getCategories(viewModel.searchPlatform)
    }
    val modloaders = remember(viewModel.searchPlatform) {
        getModloaders(viewModel.searchPlatform)
    }

    val changePlatform: (Platform) -> Unit = {
        viewModel.searchPlatform = it
        viewModel.researchWithFilter(
            viewModel.searchFilter.copy(categories = emptyList(), modloader = null)
        )
        onPlatformChange(it)
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(screenKey, currentKey, false)
    ) { isVisible ->
        // Moonlight: store layout. Search bar and quick filters on top, results in a
        // grid of cards, the full filter list in a panel that slides in from the right.
        var filtersOpen by rememberSaveable { mutableStateOf(false) }
        val filter = viewModel.searchFilter
        val activeFilters = (if (filter.gameVersion.isNotBlank()) 1 else 0) +
                filter.categories.size + (if (filter.modloader != null) 1 else 0)
        val gridState = rememberLazyGridState()
        val ctx = rememberAnimatedContext(isVisible = isVisible)
        val context = LocalContext.current
        val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

        // A new page starts at the top
        val shownPage = (viewModel.searchResult as? SearchAddonState.Success)?.page?.pageNumber
        LaunchedEffect(shownPage) {
            if (shownPage != null) gridState.scrollToItem(0)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .animated(ctx),
                isRefreshing = false,
                enabled = viewModel.searchResult !is SearchAddonState.Searching,
                onRefresh = { viewModel.search() }
            ) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 228.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "search", span = fullSpan, contentType = "search") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoonlightSearchBar(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                value = filter.searchName,
                                onValueChange = { viewModel.updateNameFilter(it) },
                                onSearch = { viewModel.resetSearch() },
                                placeholder = stringResource(platformClasses.searchHint())
                            )
                            if (enablePlatform) {
                                MoonlightPlatformToggle(
                                    modifier = Modifier.fillMaxHeight(),
                                    selected = viewModel.searchPlatform,
                                    onSelect = changePlatform
                                )
                            }
                            MoonlightFilterButton(
                                modifier = Modifier.fillMaxHeight(),
                                activeCount = activeFilters,
                                onClick = { filtersOpen = true }
                            )
                        }
                    }

                    item(key = "quick", span = fullSpan, contentType = "quick") {
                        QuickFilters(
                            filter = filter,
                            platform = viewModel.searchPlatform,
                            enableModLoader = enableModLoader,
                            modloaders = modloaders,
                            categories = categories,
                            onFilter = { viewModel.researchWithFilter(it) },
                            onVersion = { version ->
                                viewModel.updateVersionFilter(version)
                                viewModel.resetSearch()
                            }
                        )
                    }

                    when (val state = viewModel.searchResult) {
                        is SearchAddonState.Searching -> {
                            items(count = 6, key = { "skeleton$it" }, contentType = { "skeleton" }) {
                                MoonlightProjectCardSkeleton()
                            }
                        }
                        is SearchAddonState.Error -> {
                            item(key = "error", span = fullSpan, contentType = "message") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ScalingLabel(
                                        text = {
                                            AndroidStringText(
                                                text = androidText(
                                                    R.string.download_addon_failed_to_get_result,
                                                    state.message
                                                )
                                            )
                                        },
                                        onClick = { viewModel.search() }
                                    )
                                }
                            }
                        }
                        is SearchAddonState.Success -> {
                            val page = state.page
                            // APIs can repeat a project inside one page; a repeated key would crash the grid
                            val rows = page.data.distinctBy { (item, _) -> "${item.platform().name}:${item.platformId()}" }
                            if (rows.isEmpty()) {
                                item(key = "empty", span = fullSpan, contentType = "message") {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        text = stringResource(R.string.moonlight_store_no_results),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            items(
                                items = rows,
                                key = { (item, _) -> "${item.platform().name}:${item.platformId()}" },
                                contentType = { "project" }
                            ) { (item, mcmod) ->
                                val platform = remember(item) { item.platform() }
                                val title = remember(item, mcmod) { mcmod.getMcmodTitle(item.platformTitle(), context) }
                                val iconUrl = remember(item) { item.platformIconUrl() }
                                val tags = remember(item, platformClasses) {
                                    buildList {
                                        item.platformModLoaders()?.forEach { add(it.getDisplayName()) }
                                    }
                                }
                                val categoryTags = item.platformCategories(platformClasses)
                                    ?.sortedBy { it.index() }
                                    ?.map { stringResource(it.getDisplayName()) }
                                    .orEmpty()
                                val isInstalled = installedInfo?.invoke(platform, item.platformId()) != null
                                val open = { swapToDownload(platform, item.platformId(), iconUrl) }
                                MoonlightProjectCard(
                                    modifier = Modifier.animateItem(),
                                    title = title,
                                    description = item.platformDescription(),
                                    iconUrl = iconUrl,
                                    authors = item.platformAuthors(),
                                    downloads = item.platformDownloadCount(),
                                    follows = item.platformFollows(),
                                    tags = categoryTags.take(1) + tags,
                                    isInstalled = isInstalled,
                                    onOpen = open,
                                    // The project page lists the versions: the user picks the
                                    // version and the instance there, nothing installs blindly
                                    onInstall = open
                                )
                            }
                            if (rows.isNotEmpty()) {
                                item(key = "pager", span = fullSpan, contentType = "pager") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PageController(
                                            page = page,
                                            onPreviousPage = {
                                                previousPage(
                                                    pageNumber = page.pageNumber,
                                                    pages = viewModel.pages,
                                                    index = viewModel.searchFilter.index,
                                                    limit = viewModel.searchFilter.limit,
                                                    onSuccess = { previous ->
                                                        viewModel.searchResult = SearchAddonState.Success(previous)
                                                    },
                                                    onSearch = { newIndex ->
                                                        viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                                                        viewModel.search()
                                                    }
                                                )
                                            },
                                            onNextPage = {
                                                nextPage(
                                                    pageNumber = page.pageNumber,
                                                    isLastPage = page.isLastPage,
                                                    pages = viewModel.pages,
                                                    index = viewModel.searchFilter.index,
                                                    limit = viewModel.searchFilter.limit,
                                                    onSuccess = { next ->
                                                        viewModel.searchResult = SearchAddonState.Success(next)
                                                    },
                                                    onSearch = { newIndex ->
                                                        viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                                                        viewModel.search()
                                                    }
                                                )
                                            },
                                            onNavigatePage = { pageNumber ->
                                                navigatePage(
                                                    pageNumber = pageNumber,
                                                    pages = viewModel.pages,
                                                    limit = viewModel.searchFilter.limit,
                                                    onSuccess = { target ->
                                                        viewModel.searchResult = SearchAddonState.Success(target)
                                                    },
                                                    onSearch = { newIndex ->
                                                        viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                                                        viewModel.search()
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            MoonlightFilterSheet(
                visible = filtersOpen,
                onDismiss = { filtersOpen = false },
                onClear = {
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(gameVersion = "", categories = emptyList(), modloader = null)
                    )
                }
            ) {
                val searchedMcMods by viewModel.searchedMcMods.collectAsStateWithLifecycle()
                val searchedVersions by viewModel.searchedVersions.collectAsStateWithLifecycle()
                SearchFilter(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                    enablePlatform = enablePlatform,
                    searchPlatform = viewModel.searchPlatform,
                    onPlatformChange = changePlatform,
                    searchName = viewModel.searchFilter.searchName,
                    onSearchNameChange = {
                        viewModel.updateNameFilter(it)
                    },
                    onSearch = {
                        viewModel.resetSearch()
                    },
                    searchedMcMods = searchedMcMods,
                    searchedVersions = searchedVersions,
                    gameVersion = viewModel.searchFilter.gameVersion,
                    onGameVersionChange = {
                        viewModel.updateVersionFilter(it)
                    },
                    sortField = viewModel.searchFilter.sortField,
                    onSortFieldChange = {
                        viewModel.researchWithFilter(
                            viewModel.searchFilter.copy(sortField = it)
                        )
                    },
                    allCategories = categories,
                    categories = viewModel.searchFilter.categories,
                    onCategoryChanged = { categories ->
                        viewModel.researchWithFilter(
                            viewModel.searchFilter.copy(categories = categories)
                        )
                    },
                    enableModLoader = enableModLoader,
                    modloaders = modloaders,
                    modloader = viewModel.searchFilter.modloader,
                    onModLoaderChange = {
                        viewModel.researchWithFilter(
                            viewModel.searchFilter.copy(modloader = it)
                        )
                    },
                    extraFilter = extraFilter
                )
            }
        }
    }
}

/** Placeholder of the search bar for each kind of content. */
private fun PlatformClasses.searchHint(): Int = when (this) {
    PlatformClasses.MOD -> R.string.moonlight_store_search_mods
    PlatformClasses.MOD_PACK -> R.string.moonlight_store_search_modpacks
    PlatformClasses.RESOURCE_PACK -> R.string.moonlight_store_search_resource_packs
    PlatformClasses.SAVES -> R.string.moonlight_store_search_worlds
    PlatformClasses.SHADERS -> R.string.moonlight_store_search_shaders
}

/** Most used Minecraft versions, offered as one-tap pills. */
private val QuickVersions = listOf("1.21.1", "1.20.1", "1.19.2", "1.18.2", "1.16.5")

/** How many category pills are shown next to loaders and versions. */
private const val QUICK_CATEGORY_COUNT = 6

/**
 * Pills under the search bar: loaders, popular versions and the first categories.
 * Selected ones always stay visible, so a filter can be removed with one tap.
 */
@Composable
private fun QuickFilters(
    filter: PlatformSearchFilter,
    platform: Platform,
    enableModLoader: Boolean,
    modloaders: List<PlatformDisplayLabel>,
    categories: List<PlatformFilterCode>,
    onFilter: (PlatformSearchFilter) -> Unit,
    onVersion: (String) -> Unit,
) {
    val versions = remember(filter.gameVersion) {
        buildList {
            if (filter.gameVersion.isNotBlank() && filter.gameVersion !in QuickVersions) add(filter.gameVersion)
            addAll(QuickVersions)
        }
    }
    val quickCategories = remember(categories, filter.categories, platform) {
        (filter.categories + categories.filter { it !in filter.categories }).take(
            maxOf(QUICK_CATEGORY_COUNT, filter.categories.size)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (enableModLoader && modloaders.isNotEmpty()) {
            modloaders.forEach { loader ->
                val selected = filter.modloader == loader
                MoonlightPill(
                    label = loader.getDisplayName(),
                    selected = selected,
                    onClick = { onFilter(filter.copy(modloader = if (selected) null else loader)) }
                )
            }
            MoonlightPillDivider()
        }
        versions.forEach { version ->
            val selected = filter.gameVersion == version
            MoonlightPill(
                label = version,
                selected = selected,
                onClick = { onVersion(if (selected) "" else version) }
            )
        }
        if (quickCategories.isNotEmpty()) {
            MoonlightPillDivider()
            quickCategories.forEach { category ->
                val selected = category in filter.categories
                MoonlightPill(
                    label = stringResource(category.getDisplayName()),
                    selected = selected,
                    onClick = {
                        onFilter(
                            filter.copy(
                                categories = if (selected) filter.categories - category else filter.categories + category
                            )
                        )
                    }
                )
            }
        }
    }
}
