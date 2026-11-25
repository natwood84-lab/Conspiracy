package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.TheoryDossier
import com.example.security.configureAppCheck
import com.example.ui.components.ShareDossierSheet
import com.example.ui.screens.DossierDetailScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.ArchiveObsidian
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.CipherFeedTheme
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalAmberContainer
import com.example.ui.viewmodel.CipherFeedViewModel
import kotlinx.coroutines.launch

private const val ROUTE_FEED = "feed"
private const val ROUTE_VAULT = "vault"
private const val ROUTE_DOSSIER = "dossier/{dossierId}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            CipherFeedTheme {
                CipherFeedApp()
            }
        }
    }
}

@Composable
fun CipherFeedApp(
    viewModel: CipherFeedViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var shareTargetDossier by remember { mutableStateOf<TheoryDossier?>(null) }

    val dossiers by viewModel.feedDossiers.collectAsStateWithLifecycle()
    val weeklySpotlight by viewModel.weeklySpotlight.collectAsStateWithLifecycle()
    val bookmarkedDossiers by viewModel.bookmarkedDossiers.collectAsStateWithLifecycle()
    val totalBookmarkedCount by viewModel.totalBookmarkedCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.allDossiersCount.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val vaultSearchQuery by viewModel.vaultSearchQuery.collectAsStateWithLifecycle()
    val vaultSelectedTopic by viewModel.vaultSelectedTopic.collectAsStateWithLifecycle()
    val vaultTopicCounts by viewModel.vaultTopicCounts.collectAsStateWithLifecycle()
    val vaultSortOrder by viewModel.vaultSortOrder.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val feedTopicCounts by viewModel.feedTopicCounts.collectAsStateWithLifecycle()
    val onlyGpsMapped by viewModel.onlyGpsMapped.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val detailStreamState by viewModel.detailStreamState.collectAsStateWithLifecycle()
    val bookmarkFeedback by viewModel.bookmarkFeedback.collectAsStateWithLifecycle()

    LaunchedEffect(bookmarkFeedback) {
        val message = bookmarkFeedback ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearBookmarkFeedback()
    }

    val showTopLevelNav = currentRoute == ROUTE_FEED || currentRoute == ROUTE_VAULT

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = ArchiveObsidian,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useNavigationRail) {
                    NavigationBar(
                        containerColor = ArchiveSlate
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_FEED,
                            onClick = {
                                navController.navigate(ROUTE_FEED) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_FEED) Icons.Rounded.Timeline else Icons.Outlined.Timeline,
                                    contentDescription = stringResource(R.string.nav_timeline)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_timeline)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SignalAmber,
                                selectedTextColor = SignalAmber,
                                indicatorColor = SignalAmberContainer,
                                unselectedIconColor = ParchmentMuted,
                                unselectedTextColor = ParchmentMuted
                            ),
                            modifier = Modifier.testTag("nav_timeline")
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_VAULT,
                            onClick = {
                                navController.navigate(ROUTE_VAULT) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalBookmarkedCount > 0) {
                                            Badge {
                                                Text(totalBookmarkedCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_VAULT) Icons.Rounded.Bookmarks else Icons.Outlined.Bookmarks,
                                        contentDescription = stringResource(R.string.nav_vault)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_vault)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SignalAmber,
                                selectedTextColor = SignalAmber,
                                indicatorColor = SignalAmberContainer,
                                unselectedIconColor = ParchmentMuted,
                                unselectedTextColor = ParchmentMuted
                            ),
                            modifier = Modifier.testTag("nav_vault")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useNavigationRail) {
                    NavigationRail(
                        containerColor = ArchiveSlate,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_FEED,
                            onClick = {
                                navController.navigate(ROUTE_FEED) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_FEED) Icons.Rounded.Timeline else Icons.Outlined.Timeline,
                                    contentDescription = stringResource(R.string.nav_timeline)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_timeline)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SignalAmber,
                                selectedTextColor = SignalAmber,
                                indicatorColor = SignalAmberContainer,
                                unselectedIconColor = ParchmentMuted,
                                unselectedTextColor = ParchmentMuted
                            )
                        )
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_VAULT,
                            onClick = {
                                navController.navigate(ROUTE_VAULT) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalBookmarkedCount > 0) {
                                            Badge {
                                                Text(totalBookmarkedCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_VAULT) Icons.Rounded.Bookmarks else Icons.Outlined.Bookmarks,
                                        contentDescription = stringResource(R.string.nav_vault)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_vault)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SignalAmber,
                                selectedTextColor = SignalAmber,
                                indicatorColor = SignalAmberContainer,
                                unselectedIconColor = ParchmentMuted,
                                unselectedTextColor = ParchmentMuted
                            )
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_FEED,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 780.dp)
                    ) {
                        composable(ROUTE_FEED) {
                            FeedScreen(
                                dossiers = dossiers,
                                weeklySpotlight = weeklySpotlight,
                                isOnline = isOnline,
                                bookmarkedCount = totalBookmarkedCount,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                topicCounts = feedTopicCounts,
                                onlyGpsMapped = onlyGpsMapped,
                                sortOrder = sortOrder,
                                scanState = scanState,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onRunSearch = viewModel::runWebScan,
                                onScanTopic = viewModel::scanForTopic,
                                onRefreshSpotlight = viewModel::refreshSpotlight,
                                onSelectCategory = viewModel::selectCategory,
                                onToggleOnlyGpsMapped = viewModel::toggleOnlyGpsMapped,
                                onResetFilters = viewModel::resetFeedFilters,
                                onToggleSortOrder = viewModel::toggleSortOrder,
                                onToggleBookmark = viewModel::toggleBookmark,
                                onShareDossier = { shareTargetDossier = it },
                                onOpenDossier = { dossierId ->
                                    viewModel.resetDetailStream()
                                    navController.navigate("dossier/${Uri.encode(dossierId)}")
                                }
                            )
                        }
                        composable(ROUTE_VAULT) {
                            VaultScreen(
                                bookmarkedDossiers = bookmarkedDossiers,
                                totalBookmarkedCount = totalBookmarkedCount,
                                totalArchivedCount = totalCount,
                                isOnline = isOnline,
                                vaultSearchQuery = vaultSearchQuery,
                                vaultSelectedTopic = vaultSelectedTopic,
                                vaultTopicCounts = vaultTopicCounts,
                                vaultSortOrder = vaultSortOrder,
                                onVaultSearchQueryChange = viewModel::updateVaultSearchQuery,
                                onSelectVaultTopic = viewModel::selectVaultTopic,
                                onToggleVaultSortOrder = viewModel::toggleVaultSortOrder,
                                onOpenDossier = { dossierId ->
                                    viewModel.resetDetailStream()
                                    navController.navigate("dossier/${Uri.encode(dossierId)}")
                                },
                                onToggleBookmark = viewModel::toggleBookmark,
                                onShareDossier = { shareTargetDossier = it },
                                onClearUnsavedCache = viewModel::clearUnbookmarkedCache
                            )
                        }
                        composable(
                            route = ROUTE_DOSSIER,
                            arguments = listOf(navArgument("dossierId") { type = NavType.StringType })
                        ) { entry ->
                            val rawId = entry.arguments?.getString("dossierId").orEmpty()
                            val decodedId = Uri.decode(rawId)
                            val dossier by viewModel.observeDossier(decodedId)
                                .collectAsStateWithLifecycle(initialValue = null)

                            DossierDetailScreen(
                                dossier = dossier,
                                isOnline = isOnline,
                                bookmarkedCount = totalBookmarkedCount,
                                streamState = detailStreamState,
                                onBack = { navController.popBackStack() },
                                onToggleBookmark = viewModel::toggleBookmark,
                                onShareDossier = { shareTargetDossier = it },
                                onUpdateTopic = viewModel::updateDossierTopic,
                                onSaveStreamedUpdateOffline = viewModel::saveStreamedUpdateOffline,
                                onStreamUpdate = viewModel::streamLiveWebUpdate
                            )
                        }
                    }
                }
            }
        }
    }

    shareTargetDossier?.let { target ->
        ShareDossierSheet(
            dossier = target,
            onDismiss = { shareTargetDossier = null },
            onCopiedFeedback = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    }
}
