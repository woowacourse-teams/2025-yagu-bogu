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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
    val mainInitEvent = remember { MutableSharedFlow<Unit>(replay = 1) }

    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes =
                setOf(
                    Route.Main,
                    Route.Login,
                    Route.FavoriteTeam,
                ),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    googleCredentialManager = googleCredentialManager,
                    onSignIn = { navigator.navigate(Route.Main) },
                    onSignUp = { navigator.navigate(Route.FavoriteTeam) },
                )
            }
            entry<Route.Main> {
                MainScreen(
                    onSettingsClick = { navigator.navigate(Route.Setting) },
                    onBadgeClick = { navigator.navigate(Route.Badge) },
                    initEvent = mainInitEvent.asSharedFlow(),
                )
            }
            entry<Route.Setting> {
                SettingScreen(
                    onBackClick = { navigator.clearStackAndNavigate(Route.Main) },
                    onDeleteAccountCancel = { navigator.navigate(Route.Main) },
                    onFavoriteTeamEditClick = { navigator.navigate(Route.FavoriteTeam) },
                    onLogout = {
                        navigator.clearStackAndNavigate(Route.Login)
                        mainInitEvent.tryEmit(Unit)
                    },
                )
            }
            entry<Route.FavoriteTeam> {
                FavoriteTeamScreen(
                    onFavoriteTeamUpdate = {
                        navigator.navigate(Route.Main)
                    },
                )
            }
            entry<Route.Badge> {
                BadgeScreen(
                    onBackClick = { navigator.goBack() },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
