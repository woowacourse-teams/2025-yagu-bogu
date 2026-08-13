package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.dialog.LogoutDialog
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.LocalSnackbarScope
import com.yagubogu.ui.util.showSingleSnackbar
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_delete_account
import yagubogu.composeapp.generated.resources.setting_logout
import yagubogu.composeapp.generated.resources.setting_logout_alert

@Composable
fun SettingAccountScreen(
    viewModel: SettingViewModel,
    onDeleteAccountClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarScope = LocalSnackbarScope.current

    var showLogoutDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.settingEvent.collect { settingEvent ->

            if (settingEvent is SettingEvent.Logout) {
                onLogout()
                snackbarHostState.showSingleSnackbar(
                    scope = snackbarScope,
                    message = getString(Res.string.setting_logout_alert),
                )
            }
        }
    }

    SettingAccountScreen(
        onLogoutClick = { showLogoutDialog = true },
        onDeleteAccountClick = onDeleteAccountClick,
        modifier = modifier,
    )

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            onCancel = { showLogoutDialog = false },
        )
    }
}

@Composable
private fun SettingAccountScreen(
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingButtonGroup {
            SettingButton(
                text = stringResource(Res.string.setting_logout),
                onClick = onLogoutClick,
            )
            SettingButton(
                text = stringResource(Res.string.setting_delete_account),
                onClick = onDeleteAccountClick,
            )
        }
    }
}

@Preview
@Composable
private fun SettingAccountScreenPreview() {
    SettingAccountScreen(
        onLogoutClick = {},
        onDeleteAccountClick = {},
    )
}
