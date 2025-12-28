package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.yagubogu.ui.setting.component.dialog.SettingDialog
import com.yagubogu.ui.setting.component.model.SettingDialogEvent
import com.yagubogu.ui.setting.component.model.SettingEvent
import com.yagubogu.ui.theme.Gray050

@Composable
fun SettingAccountScreen(
    onClickDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val settingEvent: State<SettingEvent?> =
        viewModel.settingEvent.collectAsStateWithLifecycle(null)

    SettingAccountScreen(
        onClickLogout = { viewModel.emitDialogEvent(SettingDialogEvent.LogoutDialog) },
        onClickDeleteAccount = onClickDeleteAccount,
        settingEvent = settingEvent.value,
        modifier = modifier,
    )

    SettingDialog(viewModel = viewModel)
}

@Composable
private fun SettingAccountScreen(
    onClickLogout: () -> Unit,
    onClickDeleteAccount: () -> Unit,
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
                onClick = onClickLogout,
            )
            SettingButton(
                text = stringResource(R.string.setting_delete_account),
                onClick = onClickDeleteAccount,
            )
        }

        SettingEventHandler(settingEvent = settingEvent)
    }
}

@Preview
@Composable
private fun SettingAccountScreenPreview() {
    SettingAccountScreen(
        onClickLogout = {},
        onClickDeleteAccount = {},
        settingEvent = null,
    )
}
