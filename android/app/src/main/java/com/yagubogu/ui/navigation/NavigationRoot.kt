package com.yagubogu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.main.MainScreen
import com.yagubogu.ui.setting.SettingScreen

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = Route.BottomRoute,
            topLevelRoutes = setOf(Route.BottomRoute, Route.SettingRoute),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> =
        entryProvider {
            entry<Route.BottomRoute> {
                MainScreen(navigateToSetting = { navigator.navigate(Route.SettingRoute) })
            }
            entry<Route.SettingRoute> {
                SettingScreen(
                    navigateToParent = { navigator.clearStackAndNavigate(Route.BottomRoute) },
                    navigateToBottom = { navigator.navigate(Route.BottomRoute) },
                )
            }
        }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
