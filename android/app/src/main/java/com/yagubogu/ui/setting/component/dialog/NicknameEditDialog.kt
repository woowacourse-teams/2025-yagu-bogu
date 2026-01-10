package com.yagubogu.ui.setting.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary500

@Composable
fun NicknameEditDialog(
    nickname: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nickname: MutableState<String> = rememberSaveable { mutableStateOf(nickname) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.setting_edit_nickname),
                    style = EsamanruMedium20,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    value = nickname.value,
                    onValueChange = {
                        if (it.length <= 12) {
                            nickname.value = it
                        }
                    },
                    placeholder = { Text(text = stringResource(R.string.setting_edit_nickname_dialog_hint)) },
                    textStyle = PretendardSemiBold16,
                    modifier = Modifier.width(250.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Primary500,
                        ),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
                            text = stringResource(R.string.all_cancel),
                            style = PretendardSemiBold.copy(fontSize = 14.sp),
                        )
                    }

                    Button(
                        onClick = { onConfirm(nickname.value) },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Primary500,
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.all_confirm),
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
private fun NicknameEditDialogPreview() {
    NicknameEditDialog(
        nickname = "야구보구",
        onConfirm = {},
        onCancel = {},
    )
}
