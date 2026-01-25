package com.yagubogu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.yagubogu.ui.login.auth.GoogleCredentialManager

@Composable
fun YaguBoguRoute(
    googleCredentialManager: GoogleCredentialManager,
    startRoute: Route,
) {
    val rootNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes =
                setOf(
                    Route.Bottom,
                    Route.Login,
                    Route.FavoriteTeam,
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
    )
}
