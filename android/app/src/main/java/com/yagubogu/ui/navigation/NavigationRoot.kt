package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
    rootNavigator: Navigator,
    mainNavigator: Navigator,
    settingNavigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    googleCredentialManager = googleCredentialManager,
                    navigateToMain = { rootNavigator.navigate(Route.Bottom) },
                    navigateToFavoriteTeam = { rootNavigator.navigate(Route.FavoriteTeam) },
                )
            }
            entry<Route.Bottom> {
                MainScreen(
                    navigator = mainNavigator,
                    navigateToSetting = { rootNavigator.navigate(Route.Setting) },
                    navigateToBadge = { rootNavigator.navigate(Route.Badge) },
                )
            }
            entry<Route.Setting> {
                SettingScreen(
                    navigator = settingNavigator,
                    navigateToParent = { rootNavigator.goBack() },
                    navigateToBottom = {
                        settingNavigator.clearStack()
                        rootNavigator.clearStackAndNavigate(Route.Bottom)
                    },
                    navigateToFavoriteTeam = { rootNavigator.navigate(Route.FavoriteTeam) },
                    navigateToLogin = {
                        mainNavigator.navigate(BottomNavKey.Home)
                        settingNavigator.clearStack()
                        rootNavigator.clearStackAndNavigate(Route.Login)
                    },
                )
            }
            entry<Route.FavoriteTeam> {
                FavoriteTeamScreen(
                    navigateToMain = { rootNavigator.goBack() },
                )
            }
            entry<Route.Badge> {
                BadgeScreen(
                    navigateToMain = { rootNavigator.goBack() },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = rootNavigator.state.toEntries(entryProvider),
        onBack = { rootNavigator.goBack() },
    )
}
