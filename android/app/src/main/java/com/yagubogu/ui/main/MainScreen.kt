package com.yagubogu.ui.main

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.yagubogu.R
import com.yagubogu.presentation.setting.SettingActivity
import com.yagubogu.ui.badge.BadgeActivity
import com.yagubogu.ui.main.component.LoadingOverlay
import com.yagubogu.ui.main.component.MainNavigationBar
import com.yagubogu.ui.main.component.MainToolbar
import com.yagubogu.ui.stats.StatsScreen
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.NavigationState
import com.yagubogu.ui.util.Navigator
import com.yagubogu.ui.util.rememberNavigationState
import com.yagubogu.ui.util.toEntries
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context: Context = LocalContext.current

    val selectedItem: BottomNavKey by viewModel.selectedBottomNavKey.collectAsStateWithLifecycle()
    val isLoading: Boolean by viewModel.isLoading.collectAsStateWithLifecycle()

    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = BottomNavKey.Home,
            topLevelRoutes = BottomNavKey.items.toSet(),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }

    val reselectFlow: MutableSharedFlow<Unit> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }

    val selectedItemLabel: String = stringResource(selectedItem.label)
    LaunchedEffect(selectedItem) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "$selectedItemLabel Screen")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray050,
            topBar = {
                MainToolbar(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    title =
                        stringResource(
                            when (selectedItem) {
                                BottomNavKey.Home -> R.string.app_name
                                BottomNavKey.Livetalk,
                                BottomNavKey.Stats,
                                BottomNavKey.AttendanceHistory,
                                -> selectedItem.label
                            },
                        ),
                    onBadgeClick = { context.startActivity(BadgeActivity.newIntent(context)) },
                    onSettingsClick = { context.startActivity(SettingActivity.newIntent(context)) },
                )
            },
            bottomBar = {
                MainNavigationBar(
                    selectedItem = selectedItem,
                    onItemClick = { item: BottomNavKey ->
                        viewModel.selectBottomNavKey(item)
                        navigator.navigate(item)
                    },
                    onItemReselect = { item: BottomNavKey ->
                        reselectFlow.tryEmit(Unit)
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
                    entry<BottomNavKey.Home> { StatsScreen(snackbarHostState, reselectFlow) }
                    entry<BottomNavKey.Livetalk> { TODO("LivetalkScreen()") }
                    entry<BottomNavKey.Stats> { StatsScreen(snackbarHostState, reselectFlow) }
                    entry<BottomNavKey.AttendanceHistory> { TODO("AttendanceHistoryScreen()") }
                }

            NavDisplay(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
            )
        }
        LoadingOverlay(
            isLoading = isLoading,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
