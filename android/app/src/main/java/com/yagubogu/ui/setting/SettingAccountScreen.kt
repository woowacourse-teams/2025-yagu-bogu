package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.SettingEventHandler
import com.yagubogu.ui.setting.component.dialog.LogoutDialog
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050

@Composable
fun SettingAccountScreen(
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    var showLogoutDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val settingEvent: State<SettingEvent?> =
        viewModel.settingEvent.collectAsStateWithLifecycle(null)

    SettingAccountScreen(
        onLogoutClick = { showLogoutDialog = true },
        onDeleteAccountClick = onDeleteAccountClick,
        settingEvent = settingEvent.value,
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
    settingEvent: SettingEvent?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(20.dp)
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

        SettingEventHandler(settingEvent = settingEvent)
    }
}

@Preview
@Composable
private fun SettingAccountScreenPreview() {
    SettingAccountScreen(
        onLogoutClick = {},
        onDeleteAccountClick = {},
        settingEvent = null,
    )
}
