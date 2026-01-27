package com.yagubogu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.yagubogu.ui.login.auth.GoogleCredentialManager

/**
 * 앱의 최상위 라우팅 컴포저블.
 *
 * 각 네비게이션 상태를 별도로 관리하여 스택 추적 독립성을 보장합니다.
 * [rootNavigator]: 최상위 라우팅 관리
 * [mainNavigator]: 하단 탭 네비게이션
 * [settingNavigator]: 설정 화면 네비게이션
 *
 * @param googleCredentialManager 구글 인증 관리자
 * @param startRoute 앱 시작 시 표시할 화면
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun YaguBoguRoute(
    googleCredentialManager: GoogleCredentialManager,
    startRoute: Route,
    modifier: Modifier = Modifier,
) {
    val rootNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes =
                setOf(
                    Route.Main,
                    Route.Login,
                    Route.FavoriteTeam,
                    Route.Setting,
                ),
        )
    val rootNavigator: Navigator = remember { Navigator(rootNavigationState) }

    val mainNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = BottomNavKey.Home,
            topLevelRoutes = BottomNavKey.items.toSet(),
        )
    val mainNavigator: Navigator = remember { Navigator(mainNavigationState) }

    val settingNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = SettingNavKey.SettingMain,
            topLevelRoutes = setOf(SettingNavKey.SettingMain),
        )
    val settingNavigator: Navigator = remember { Navigator(settingNavigationState) }

    NavigationRoot(
        googleCredentialManager = googleCredentialManager,
        rootNavigator = rootNavigator,
        mainNavigator = mainNavigator,
        settingNavigator = settingNavigator,
        modifier = modifier,
    )
}
