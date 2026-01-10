package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.presentation.login.auth.GoogleCredentialManager
import com.yagubogu.ui.favorite.FavoriteTeamScreen
import com.yagubogu.ui.login.LoginScreen
import com.yagubogu.ui.main.MainScreen

@Composable
fun NavigationRoot(
    googleCredentialManager: GoogleCredentialManager,
    modifier: Modifier = Modifier,
) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = Route.LoginRoute,
            topLevelRoutes = setOf(Route.BottomRoute, Route.LoginRoute),
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
                MainScreen()
            }
            entry<Route.FavoriteTeamRoute> {
                FavoriteTeamScreen(
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
