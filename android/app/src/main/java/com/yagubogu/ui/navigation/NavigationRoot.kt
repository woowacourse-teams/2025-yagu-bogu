package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.main.MainScreen
import com.yagubogu.ui.setting.SettingScreen
import com.yagubogu.ui.setting.SettingViewModel
import com.yagubogu.ui.setting.component.SettingEventHandler

/**
 * 앱의 최상위 네비게이션 구조를 정의하는 루트 컴포저블.
 *
 * 각 경로([Route])에 따른 화면 컴포저블을 매핑하여 화면 전환을 관리합니다.
 * 자식 컴포저블과 분리된 독립적인 [Navigator]를 사용합니다.
 *
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = Route.Bottom,
            topLevelRoutes = setOf(Route.Bottom, Route.Setting),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: SettingViewModel = hiltViewModel()

    val isMainTab = navigationState.topLevelRoute == Route.Bottom
    val snackbarBottomPadding = if (isMainTab) 92.dp else 20.dp

    SettingEventHandler(
        settingEvent = viewModel.settingEvent,
        snackbarHostState = snackbarHostState,
        navigateToHome = { navigator.clearStackAndNavigate(Route.Bottom) },
    )

    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.Bottom> {
                MainScreen(navigateToSetting = { navigator.navigate(Route.Setting) })
            }
            entry<Route.Setting> {
                SettingScreen(
                    navigateToParent = { navigator.clearStackAndNavigate(Route.Bottom) },
                )
            }
        }
    Box(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = snackbarBottomPadding),
        ) {
            Snackbar(snackbarData = it)
        }
    }
}
