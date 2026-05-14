package com.yagubogu.ui.login.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.home.model.MaintenanceInfo
import com.yagubogu.ui.home.model.PopupNoticeInfo
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.dialog_ignore_days_label

@Composable
fun PopupNoticeDialog(
    onConfirm: (Boolean) -> Unit,
    popupNoticeInfo: PopupNoticeInfo,
    modifier: Modifier = Modifier,
) {
    var isChecked by remember { mutableStateOf(false) }

    val dialogUiModel =
        DefaultDialogUiModel(
            title = popupNoticeInfo.title ?: "",
            emoji = popupNoticeInfo.emoji,
            message = popupNoticeInfo.message,
            textAlign = popupNoticeInfo.textAlign,
        )

    DefaultDialog(
        dialogUiModel = dialogUiModel,
        onConfirm = { onConfirm(isChecked) },
        onCancel = {},
        modifier = modifier,
        bottomContent =
            popupNoticeInfo.skippableDays?.takeIf { it > 0 }?.let { days ->
                {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .noRippleClickable { isChecked = !isChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor = Primary500,
                                    uncheckedColor = Gray200,
                                ),
                        )
                        Text(
                            text = stringResource(Res.string.dialog_ignore_days_label, days),
                            style = PretendardMedium.copy(fontSize = 14.sp),
                            color = Gray700,
                        )
                    }
                }
            },
    )
}

private val MAINTENANCE_INFO =
    MaintenanceInfo(
        id = 1,
        remoteIsShow = true,
        shouldShowPopup = true,
        emoji = "🚧",
        title = "야구보구 점검중 테스트",
        message = "점검중입니다. 잠시만 기다려주세요.\n점검중입니다. 잠시만 기다려주세요.",
        textAlign = null,
        skippableDays = 3,
        isLoginBlock = false,
    )

@Preview
@Composable
private fun PopupNoticeDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        PopupNoticeDialog(
            onConfirm = {},
            popupNoticeInfo = MAINTENANCE_INFO,
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun PopupNoticeDialogPreviewNoSkippableDays() {
    Box(modifier = Modifier.fillMaxSize()) {
        PopupNoticeDialog(
            onConfirm = {},
            popupNoticeInfo = MAINTENANCE_INFO.copy(skippableDays = null),
            modifier = Modifier,
        )
    }
}
