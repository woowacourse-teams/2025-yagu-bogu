package com.yagubogu.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.AttendanceHistoryScreen
import com.yagubogu.ui.home.HomeScreen
import com.yagubogu.ui.livetalk.LivetalkScreen
import com.yagubogu.ui.main.component.LoadingOverlay
import com.yagubogu.ui.main.component.MainNavigationBar
import com.yagubogu.ui.main.component.MainToolbar
import com.yagubogu.ui.navigation.model.BottomNavKey
import com.yagubogu.ui.navigation.model.NavigationState
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.stats.StatsScreen
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.fadeTransition
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.app_name

@Composable
fun MainScreen(
    navigationState: NavigationState,
    onBackClick: () -> Unit,
    onBottomItemClick: (BottomNavKey) -> Unit,
    onSettingsClick: () -> Unit,
    onBadgeClick: () -> Unit,
    onRankingShowMoreClick: (RankingType) -> Unit,
    onLivetalkItemClick: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
) {
    val selectedItem: BottomNavKey by viewModel.selectedBottomNavKey.collectAsStateWithLifecycle()
    val isLoading: Boolean by viewModel.isLoading.collectAsStateWithLifecycle()

    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scrollToTopEvent = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    LaunchedEffect(Unit) {
        viewModel.selectBottomNavKey(
            navigationState.currentRoute as? BottomNavKey ?: BottomNavKey.Home,
        )
    }

    val selectedItemLabel: String = stringResource(selectedItem.label)
    LaunchedEffect(selectedItem) {
        AnalyticsLogger.logEvent("screen_view", mapOf("screen_name" to "$selectedItemLabel Screen"))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray050,
            topBar = {
                MainToolbar(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    title =
                        stringResource(
                            if (selectedItem == BottomNavKey.Home) {
                                Res.string.app_name
                            } else {
                                selectedItem.label
                            },
                        ),
                    onBadgeClick = { onBadgeClick() },
                    onSettingsClick = { onSettingsClick() },
                )
            },
            bottomBar = {
                MainNavigationBar(
                    selectedItem = selectedItem,
                    onItemClick = { item: BottomNavKey ->
                        viewModel.selectBottomNavKey(item)
                        onBottomItemClick(item)
                    },
                    onItemReselect = { item: BottomNavKey ->
                        scrollToTopEvent.tryEmit(Unit)
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = {
                        Snackbar(
                            snackbarData = it,
                            containerColor = Color.DarkGray,
                            contentColor = White,
                        )
                    },
                )
            },
        ) { innerPadding: PaddingValues ->
            val entryProvider: (NavKey) -> NavEntry<NavKey> =
                entryProvider {
                    entry<BottomNavKey.Home> {
                        HomeScreen(
                            scrollToTopEvent = scrollToTopEvent,
                            onLoading = viewModel::setLoading,
                            onRankingShowMoreClick = onRankingShowMoreClick,
                        )
                    }
                    entry<BottomNavKey.Livetalk> {
                        LivetalkScreen(
                            scrollToTopEvent = scrollToTopEvent,
                            onLivetalkItemClick = onLivetalkItemClick,
                        )
                    }
                    entry<BottomNavKey.Stats> {
                        StatsScreen(
                            scrollToTopEvent = scrollToTopEvent,
                        )
                    }
                    entry<BottomNavKey.AttendanceHistory> {
                        AttendanceHistoryScreen(
                            scrollToTopEvent = scrollToTopEvent,
                        )
                    }
                }

            NavDisplay(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                entries = navigationState.toEntries(entryProvider),
                onBack = onBackClick,
                transitionSpec = { fadeTransition() },
                popTransitionSpec = { fadeTransition() },
            )
        }
        LoadingOverlay(isLoading = isLoading)
    }
}
