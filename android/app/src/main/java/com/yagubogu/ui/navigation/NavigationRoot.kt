package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.favorite.FavoriteTeamScreen
import com.yagubogu.ui.login.LoginScreen
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.main.MainScreen
import com.yagubogu.ui.setting.SettingScreen

/**
 * 앱의 최상위 네비게이션 구조를 정의하는 루트 컴포저블.
 *
 * 각 경로([Route])에 따른 화면 컴포저블을 매핑하여 화면 전환을 관리합니다.
 * 자식 컴포저블과 분리된 독립적인 [Navigator]를 사용합니다.
 *
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun NavigationRoot(
    googleCredentialManager: GoogleCredentialManager,
    startRoute: Route,
    modifier: Modifier = Modifier,
) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes =
                setOf(
                    Route.Bottom,
                    Route.Login,
                    Route.Setting,
                    Route.FavoriteTeam,
                ),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    googleCredentialManager = googleCredentialManager,
                    navigateToMain = { navigator.navigate(Route.Bottom) },
                    navigateToFavoriteTeam = { navigator.navigate(Route.FavoriteTeam) },
                )
            }
            entry<Route.Bottom> {
                MainScreen(
                    navigateToSetting = { navigator.navigate(Route.Setting) },
                    navigateToBadge = { navigator.navigate(Route.Badge) },
                )
            }
            entry<Route.Setting> {
                SettingScreen(
                    navigateToParent = { navigator.clearStackAndNavigate(Route.Bottom) },
                    navigateToBottom = { navigator.navigate(Route.Bottom) },
                    navigateToFavoriteTeam = { navigator.navigate(Route.FavoriteTeam) },
                    navigateToLogin = { navigator.clearStackAndNavigate(Route.Login) },
                )
            }
            entry<Route.FavoriteTeam> {
                FavoriteTeamScreen(
                    navigateToMain = {
                        navigator.navigate(Route.Bottom)
                    },
                )
            }
            entry<Route.Badge> {
                BadgeScreen(
                    navigateToMain = { navigator.goBack() },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
