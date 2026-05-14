package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.ui.common.model.DefaultDialogUiModel
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_confirm

@Composable
fun DefaultDialog(
    dialogUiModel: DefaultDialogUiModel,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    DefaultDialog(
        negativeText = dialogUiModel.negativeText,
        positiveText = dialogUiModel.positiveText,
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
    ) {
        dialogUiModel.emoji?.let { emoji: String ->
            Text(
                text = emoji,
                style = TextStyle(fontSize = 48.sp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dialogUiModel.title,
            style = EsamanruMedium20,
            textAlign = TextAlign.Center,
        )

        dialogUiModel.message?.let { message: String ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = PretendardMedium.copy(fontSize = 14.sp),
                textAlign = dialogUiModel.textAlign.toTextAlign(),
                color = Gray700,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        bottomContent?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDialog(
    negativeText: String?,
    positiveText: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onCancel,
        modifier = modifier.fillMaxWidth(0.9f),
        properties =
            DialogProperties(
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    negativeText?.let { negativeText: String ->
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Gray200,
                                    contentColor = Gray500,
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = negativeText,
                                style = PretendardSemiBold.copy(fontSize = 14.sp),
                            )
                        }
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Primary500,
                                contentColor = White,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text =
                                positiveText
                                    ?: stringResource(Res.string.all_confirm),
                            style = PretendardSemiBold.copy(fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

private fun String?.toTextAlign(): TextAlign =
    when {
        this == "Start" -> TextAlign.Start
        this == "End" -> TextAlign.End
        this == "Justify" -> TextAlign.Justify
        else -> TextAlign.Center
    }

@Preview
@Composable
private fun DefaultDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        DefaultDialog(
            dialogUiModel =
                DefaultDialogUiModel(
                    title = "잠실야구장\n직관 인증할까요?",
                    emoji = "🏟️",
                    message = "직관 통계는 매일 자정에 자동 반영돼요.\n응원팀 경기가 아니면 인증 횟수에만 집계돼요.",
                    negativeText = "취소",
                ),
            onConfirm = {},
            onCancel = {},
        )
    }
}
