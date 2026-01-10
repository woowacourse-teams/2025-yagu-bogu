package com.yagubogu.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.navigation.NavigationState
import com.yagubogu.ui.navigation.Navigator
import com.yagubogu.ui.navigation.SettingNavKey
import com.yagubogu.ui.navigation.rememberNavigationState
import com.yagubogu.ui.navigation.toEntries
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.White

@Composable
fun SettingScreen(
    parentNavigator: Navigator,
    navigateToParent: () -> Unit,
    navigateToBottom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = SettingNavKey.SettingMain,
            topLevelRoutes = setOf(SettingNavKey.SettingMain),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray050,
            topBar = {
                DefaultToolbar(
                    onBackClick = {
                        when (navigator.canGoBack()) {
                            true -> navigator.goBack()
                            false -> navigateToParent()
                        }
                    },
                    title = stringResource((navigator.currentRoute as SettingNavKey).label),
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = {
                        Snackbar(
                            snackbarData = it,
                            containerColor = Color.DarkGray,
                            contentColor = White,
                        )
                    },
                )
            },
        ) { innerPadding: PaddingValues ->
            val entryProvider: (NavKey) -> NavEntry<NavKey> =
                entryProvider {
                    entry<SettingNavKey.SettingMain> {
                        SettingMainScreen(
                            onClickSettingAccount = { navigator.navigate(SettingNavKey.SettingAccount) },
                            onFavoriteTeamEditClick = { parentNavigator.navigate(Route.FavoriteTeamRoute) },
                        )
                    }
                    entry<SettingNavKey.SettingAccount> {
                        SettingAccountScreen(
                            onClickDeleteAccount = { navigator.navigate(SettingNavKey.SettingDeleteAccount) },
                            navigateToLogin = {
                                navigator.clearStack()
                                parentNavigator.clearStackAndNavigate(Route.LoginRoute)
                            },
                        )
                    }
                    entry<SettingNavKey.SettingDeleteAccount> {
                        SettingDeleteAccountScreen(
                            navigateToHome = {
                                navigator.clearStack()
                                navigateToBottom()
                            },
                            navigateToLogin = {
                                navigator.clearStack()
                                parentNavigator.clearStackAndNavigate(Route.LoginRoute)
                            },
                        )
                    }
                }

            NavDisplay(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
            )
        }
    }
}
