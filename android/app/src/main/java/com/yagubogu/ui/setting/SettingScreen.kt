package com.yagubogu.ui.setting

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
import com.yagubogu.R
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.navigation.Navigator
import com.yagubogu.ui.navigation.SettingNavKey
import com.yagubogu.ui.navigation.toEntries
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.White

@Composable
fun SettingScreen(
    navigator: Navigator,
    navigateToParent: () -> Unit,
    navigateToBottom: () -> Unit,
    navigateToFavoriteTeam: () -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

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
                title =
                    stringResource(
                        (navigator.currentRoute as? SettingNavKey)?.label
                            ?: R.string.setting_main_title,
                    ),
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
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<SettingNavKey.SettingMain> {
                    SettingMainScreen(
                        onClickSettingAccount = { navigator.navigate(SettingNavKey.SettingAccount) },
                        onFavoriteTeamEditClick = navigateToFavoriteTeam,
                    )
                }
                entry<SettingNavKey.SettingAccount> {
                    SettingAccountScreen(
                        onDeleteAccountClick = { navigator.navigate(SettingNavKey.SettingDeleteAccount) },
                        navigateToLogin = navigateToLogin,
                    )
                }
                entry<SettingNavKey.SettingDeleteAccount> {
                    SettingDeleteAccountScreen(
                        navigateToHome = navigateToBottom,
                        navigateToLogin = navigateToLogin,
                    )
                }
            }

        NavDisplay(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            entries = navigator.state.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )
    }
}
