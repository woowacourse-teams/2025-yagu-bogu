package com.yagubogu.ui.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var isTopBarVisible by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Gray050,
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                DefaultToolbar(
                    onBackClick = {
                        when (navigator.canGoBack()) {
                            true -> navigator.goBack()
                            false -> navigateToParent()
                        }
                    },
                    title = stringResource((navigator.currentRoute as SettingNavKey).label),
                )
            }
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
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<SettingNavKey.SettingMain> {
                    SettingMainScreen(
                        onClickSettingAccount = { navigator.navigate(SettingNavKey.SettingAccount) },
                        onFullScreenMode = { isFull -> isTopBarVisible = !isFull },
                    )
                }
                entry<SettingNavKey.SettingAccount> {
                    SettingAccountScreen(
                        onDeleteAccountClick = { navigator.navigate(SettingNavKey.SettingDeleteAccount) },
                    )
                }
                entry<SettingNavKey.SettingDeleteAccount> {
                    SettingDeleteAccountScreen(navigateToHome = {
                        navigator.clearStack()
                        navigateToBottom()
                    })
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
