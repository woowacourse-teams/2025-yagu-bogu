package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.runtime.Composable
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenActions
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenStates
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.livetalk_trash_btn
import yagubogu.composeapp.generated.resources.livetalk_trash_dialog_message
import yagubogu.composeapp.generated.resources.livetalk_user_report_btn
import yagubogu.composeapp.generated.resources.livetalk_user_report_dialog_message

@Composable
fun LivetalkChatDialogs(
    state: LivetalkChatScreenStates.Dialog,
    actions: LivetalkChatScreenActions.Dialog,
) {
    // 삭제 다이얼로그
    state.pendingDeleteChat?.let { chat ->
        DefaultDialog(
            dialogUiModel =
                DefaultDialogUiModel(
                    title = stringResource(Res.string.livetalk_trash_btn),
                    message = stringResource(Res.string.livetalk_trash_dialog_message),
                    positiveText = stringResource(Res.string.livetalk_trash_btn),
                    negativeText = stringResource(Res.string.all_cancel),
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
                    title = stringResource(Res.string.livetalk_user_report_btn),
                    message =
                        stringResource(
                            Res.string.livetalk_user_report_dialog_message,
                            chat.nickname ?: "",
                        ),
                    positiveText = stringResource(Res.string.livetalk_user_report_btn),
                    negativeText = stringResource(Res.string.all_cancel),
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
