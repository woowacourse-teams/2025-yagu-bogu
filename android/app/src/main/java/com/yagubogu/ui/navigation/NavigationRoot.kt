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

@Composable
fun NavigationRoot(
    googleCredentialManager: GoogleCredentialManager,
    startRoute: Route,
    modifier: Modifier = Modifier,
) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes = setOf(Route.BottomRoute, Route.LoginRoute, Route.SettingRoute),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.LoginRoute> {
                LoginScreen(
                    googleCredentialManager = googleCredentialManager,
                    navigateToMain = { navigator.navigate(Route.BottomRoute) },
                    navigateToFavoriteTeam = { navigator.navigate(Route.FavoriteTeamRoute) },
                )
            }
            entry<Route.BottomRoute> {
                MainScreen(
                    navigateToSetting = { navigator.navigate(Route.SettingRoute) },
                    navigateToBadge = { navigator.navigate(Route.BadgeRoute) },
                )
            }
            entry<Route.SettingRoute> {
                SettingScreen(
                    navigateToParent = { navigator.goBack() },
                    navigateToBottom = { navigator.navigate(Route.BottomRoute) },
                    navigateToFavoriteTeam = { navigator.navigate(Route.FavoriteTeamRoute) },
                    navigateToLogin = { navigator.navigate(Route.LoginRoute) },
                )
            }
            entry<Route.FavoriteTeamRoute> {
                FavoriteTeamScreen(
                    navigateToMain = { navigator.navigate(Route.BottomRoute) },
                )
            }
            entry<Route.BadgeRoute> {
                BadgeScreen(
                    navigateToMain = { navigator.navigate(Route.BottomRoute) },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
