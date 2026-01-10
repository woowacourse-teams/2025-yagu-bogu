package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenActions
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenStates

@Composable
fun LivetalkChatDialogs(
    state: LivetalkChatScreenStates,
    actions: LivetalkChatScreenActions.Dialog,
) {
    // 삭제 다이얼로그
    state.pendingDeleteChat?.let { chat ->
        DefaultDialog(
            dialogUiModel =
                DefaultDialogUiModel(
                    title = stringResource(R.string.livetalk_trash_btn),
                    message = stringResource(R.string.livetalk_trash_dialog_message),
                    positiveText = stringResource(R.string.livetalk_trash_btn),
                    negativeText = stringResource(R.string.all_cancel),
                ),
            onConfirm = { actions.onDeleteMessage(chat.chatId) },
            onCancel = actions.onDismissDeleteDialog,
        )
    }

    // 신고 다이얼로그
    state.pendingReportChat?.let { chat ->
        DefaultDialog(
            dialogUiModel =
                DefaultDialogUiModel(
                    title = stringResource(R.string.livetalk_user_report_btn),
                    message =
                        stringResource(
                            R.string.livetalk_user_report_dialog_message,
                            chat.nickname ?: "",
                        ),
                    positiveText = stringResource(R.string.livetalk_user_report_btn),
                    negativeText = stringResource(R.string.all_cancel),
                ),
            onConfirm = { actions.onReportMessage(chat.chatId) },
            onCancel = actions.onDismissReportDialog,
        )
    }

    // 프로필 다이얼로그
    state.clickedProfile?.let { profile ->
        ProfileDialog(
            onDismissRequest = { actions.onDismissProfile() },
            memberProfile = profile,
        )
    }
}
