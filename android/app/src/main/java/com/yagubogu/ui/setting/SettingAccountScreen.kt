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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.dialog.LogoutDialog
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050

@Composable
fun SettingAccountScreen(
    onDeleteAccountClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var showLogoutDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val settingEvent: SettingEvent? by viewModel.settingEvent.collectAsStateWithLifecycle(null)

    LaunchedEffect(settingEvent) {
        if (settingEvent is SettingEvent.Logout) {
            onLogout()
            context.showToast(R.string.setting_logout_alert)
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
                text = stringResource(R.string.setting_logout),
                onClick = onLogoutClick,
            )
            SettingButton(
                text = stringResource(R.string.setting_delete_account),
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
