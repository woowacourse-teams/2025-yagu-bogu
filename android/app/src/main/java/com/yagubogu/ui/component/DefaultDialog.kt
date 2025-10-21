import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500

@Composable
fun DefaultDialog(
    dialogUiModel: DefaultDialogUiModel,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                )

                dialogUiModel.message?.let { message: String ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = message,
                        style = PretendardMedium.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                        color = Gray700,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    dialogUiModel.negativeText?.let { negativeText: String ->
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
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text =
                                dialogUiModel.positiveText
                                    ?: stringResource(R.string.all_confirm),
                            style = PretendardSemiBold.copy(fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DefaultDialogPreview() {
    DefaultDialog(
        dialogUiModel =
            DefaultDialogUiModel(
                title = "야구장에서 인증할까요?",
                emoji = "🏟️",
                message = "직관 통계는 매일 자정에 자동 반영돼요.\n응원팀 경기가 아니면 인증 횟수에만 집계돼요.",
                negativeText = "취소",
            ),
        onConfirm = {},
        onCancel = {},
    )
}
