package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.livetalk.chat.LivetalkChatScreen
import com.yagubogu.ui.login.LoginScreen
import com.yagubogu.ui.main.MainScreen
import com.yagubogu.ui.navigation.model.BottomNavKey
import com.yagubogu.ui.navigation.model.Navigator
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.onboarding.favorite.FavoriteTeamScreen
import com.yagubogu.ui.onboarding.nickname.NicknameScreen
import com.yagubogu.ui.ranking.component.RankingScreen
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.setting.SettingScreen
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.LocalSnackbarScope
import com.yagubogu.ui.util.slidePopTransition
import com.yagubogu.ui.util.slidePredictivePopTransition
import com.yagubogu.ui.util.slidePushTransition
import com.yagubogu.ui.util.snackbarPadding
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * 앱의 최상위 네비게이션 구조를 정의하는 루트 컴포저블.
 *
 * 각 경로([Route])에 따른 화면 컴포저블을 매핑하여 화면 전환을 관리합니다.
 *
 * @param rootNavigator 최상위 라우팅 관리 Navigator
 * @param mainNavigator 하단 탭 네비게이션 Navigator
 * @param settingNavigator 설정 화면 네비게이션 Navigator
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun NavigationRoot(
    rootNavigator: Navigator,
    mainNavigator: Navigator,
    settingNavigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalSnackbarScope provides snackbarScope,
    ) {
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<Route.Login> {
                    LoginScreen(
                        onSignIn = { rootNavigator.navigate(Route.Main) },
                        onSignUp = { rootNavigator.navigate(Route.FavoriteTeam(isOnboarding = true)) },
                    )
                }
                entry<Route.Main> {
                    MainScreen(
                        navigationState = mainNavigator.state,
                        onBackClick = { mainNavigator.goBack() },
                        onBottomItemClick = { item: BottomNavKey ->
                            mainNavigator.navigate(item)
                        },
                        onSettingsClick = { rootNavigator.navigate(Route.Setting) },
                        onBadgeClick = { rootNavigator.navigate(Route.Badge) },
                        onRankingShowMoreClick = { type: RankingType ->
                            rootNavigator.navigate(Route.Ranking(type))
                        },
                        onLivetalkItemClick = { gameId: Long, isVerified: Boolean ->
                            rootNavigator.navigate(Route.LivetalkChat(gameId, isVerified))
                        },
                    )
                }
                entry<Route.Setting> {
                    SettingScreen(
                        navigationState = settingNavigator.state,
                        onBackClick = {
                            when (settingNavigator.canGoBack()) {
                                true -> settingNavigator.goBack()
                                false -> rootNavigator.goBack()
                            }
                        },
                        onSettingItemClick = { item: SettingNavKey ->
                            settingNavigator.navigate(item)
                        },
                        onFavoriteTeamEditClick = { rootNavigator.navigate(Route.FavoriteTeam(isOnboarding = false)) },
                        onLogout = {
                            mainNavigator.navigate(BottomNavKey.Home)
                            settingNavigator.clearStack()
                            rootNavigator.clearStack()
                            rootNavigator.navigate(Route.Login)
                        },
                        onDeleteAccountCancel = {
                            mainNavigator.navigate(BottomNavKey.Home)
                            settingNavigator.clearStack()
                            rootNavigator.clearStack()
                            rootNavigator.navigate(Route.Main)
                        },
                        onDeleteAccount = {
                            mainNavigator.navigate(BottomNavKey.Home)
                            settingNavigator.clearStack()
                            rootNavigator.clearStack()
                            rootNavigator.navigate(Route.Login)
                        },
                    )
                }
                entry<Route.FavoriteTeam> { key: Route.FavoriteTeam ->
                    FavoriteTeamScreen(
                        onConfirmClickToNicknameSetup = { teamName: String ->
                            rootNavigator.navigate(Route.NicknameSetup(teamName))
                        },
                        onConfirmClickToNavigateHome = {
                            mainNavigator.navigate(BottomNavKey.Home)
                            rootNavigator.clearStack()
                            rootNavigator.navigate(Route.Main)
                        },
                        viewModel =
                            koinViewModel(
                                parameters = { parametersOf(key.isOnboarding) },
                            ),
                    )
                }
                entry<Route.NicknameSetup> { key: Route.NicknameSetup ->
                    NicknameScreen(
                        viewModel =
                            koinViewModel(
                                parameters = { parametersOf(key.teamName) },
                            ),
                        onCompleted = {
                            mainNavigator.navigate(BottomNavKey.Home)
                            rootNavigator.clearStack()
                            rootNavigator.navigate(Route.Main)
                        },
                    )
                }
                entry<Route.Badge> {
                    BadgeScreen(
                        onBackClick = { rootNavigator.goBack() },
                    )
                }
                entry<Route.Ranking> { key: Route.Ranking ->
                    RankingScreen(
                        type = key.type,
                        onBackClick = { rootNavigator.goBack() },
                    )
                }
                entry<Route.LivetalkChat> { key: Route.LivetalkChat ->
                    LivetalkChatScreen(
                        gameId = key.gameId,
                        isVerified = key.isVerified,
                        onBackClick = { rootNavigator.goBack() },
                    )
                }
            }
        Box(modifier = modifier.fillMaxSize()) {
            // 외부에서 온 modifier 설정을 소비
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                entries = rootNavigator.state.toEntries(entryProvider),
                onBack = { rootNavigator.goBack() },
                transitionSpec = { slidePushTransition() },
                popTransitionSpec = { slidePopTransition() },
                predictivePopTransitionSpec = { edge ->
                    slidePredictivePopTransition(edge)
                },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = snackbarPadding(isMainScreen = rootNavigator.state.currentRoute is Route.Main)),
            ) {
                Snackbar(snackbarData = it)
            }
        }
    }
}
