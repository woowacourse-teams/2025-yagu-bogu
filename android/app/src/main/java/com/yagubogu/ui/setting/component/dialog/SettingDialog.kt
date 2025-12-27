package com.yagubogu.ui.setting.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.presentation.setting.SettingViewModel
import com.yagubogu.ui.setting.component.model.SettingDialogEvent

@Composable
fun SettingDialog(
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier,
) {
    val dialogEvent: SettingDialogEvent by viewModel.dialogEvent.collectAsStateWithLifecycle(
        initialValue = SettingDialogEvent.HideDialog,
    )
    when (dialogEvent) {
        SettingDialogEvent.DeleteAccountDialog -> {
            DeleteAccountDialog(
                onConfirm = viewModel::deleteAccount,
                onCancel = viewModel::cancelDeleteAccount,
            )
        }

        SettingDialogEvent.LogoutDialog -> {
            LogoutDialog(
                onConfirm = viewModel::logout,
                onCancel = viewModel::hideDialog,
            )
        }

        SettingDialogEvent.NicknameEditDialog -> {
            NicknameEditDialog(
                nickname = viewModel.myMemberInfoItem.value.nickName,
                onConfirm = viewModel::updateNickname,
                onCancel = viewModel::hideDialog,
            )
        }

        SettingDialogEvent.HideDialog -> {
            Unit
        }
    }
}
