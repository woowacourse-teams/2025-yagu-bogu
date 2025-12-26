package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.Primary400
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.shimmerIf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatInputBar(
    messageFormText: String,
    stadiumName: String?,
    isVerified: Boolean,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 16.dp,
                ).imePadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val placeholderText =
            when {
                isVerified && !stadiumName.isNullOrEmpty() -> {
                    stringResource(
                        id = R.string.livetalk_chat_input_hint,
                        stadiumName,
                    )
                }

                else -> {
                    stringResource(id = R.string.livetalk_chat_input_hint_unverified)
                }
            }

        BasicTextField(
            value = messageFormText,
            onValueChange = onTextChange,
            enabled = isVerified,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerIf(stadiumName == null)
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp),
            textStyle = PretendardRegular16,
            maxLines = 4,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = messageFormText,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            placeholderText,
                            style = PretendardRegular16,
                            color = Color.Gray,
                        )
                    },
                    singleLine = false,
                    enabled = true,
                    interactionSource = remember { MutableInteractionSource() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = remember { MutableInteractionSource() },
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Gray100,
                                    unfocusedContainerColor = Gray100,
                                    focusedBorderColor = Gray300,
                                    unfocusedBorderColor = Gray300,
                                ),
                            shape = RoundedCornerShape(12.dp),
                            focusedBorderThickness = 1.dp,
                            unfocusedBorderThickness = 1.dp,
                        )
                    },
                )
            },
        )

        Spacer(Modifier.width(8.dp))

        val sendBtnBackgroundColor =
            when {
                // 입력 O + 전송 가능
                messageFormText.isNotEmpty() -> Primary500

                // 입력 X + 전송 가능 (빈 메시지?)
                isVerified -> Primary400

                // 전송 불가 (잠금 상태)
                else -> Gray400
            }
        val sendIconResource = if (isVerified) R.drawable.ic_send else R.drawable.ic_lock

        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .align(Alignment.Bottom)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerIf(stadiumName == null)
                    .background(sendBtnBackgroundColor)
                    .clickable(
                        enabled = messageFormText.isNotEmpty(),
                        onClick = onSendMessage,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = sendIconResource),
                contentDescription =
                    when (isVerified) {
                        true -> stringResource(id = R.string.livetalk_send_btn_description)
                        false -> stringResource(id = R.string.livetalk_send_btn_locked_description)
                    },
                tint = White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun LivetalkChatInputBarPreviewShimmerInput() {
    LivetalkChatInputBar("", null, isVerified = true, onTextChange = {}, onSendMessage = {})
}

@Preview
@Composable
private fun LivetalkChatInputBarPreviewEmptyInput() {
    LivetalkChatInputBar("", "고척 스카이돔", isVerified = true, onTextChange = {}, onSendMessage = {})
}

@Preview
@Composable
private fun LivetalkChatInputBarPreviewOneLineInput() {
    LivetalkChatInputBar(
        "한화 이겨라",
        "고척 스카이돔",
        isVerified = true,
        onTextChange = {},
        onSendMessage = {},
    )
}

@Preview
@Composable
private fun LivetalkChatInputBarPreviewMultiLineInput() {
    LivetalkChatInputBar(
        "한화의 김성근 감독님 사랑해 예 예 예 예예예 예 예 예 예예예 예 예 예 예예예 예 예 예 예예예 예 예 예 예예예 예 예 예 예예예 예 예 예 예예예 예~ 한화의 김성근 감독님 사랑해",
        "고척 스카이돔",
        isVerified = true,
        onTextChange = {},
        onSendMessage = {},
    )
}

@Preview
@Composable
private fun LivetalkChatInputBarPreviewNotVerified() {
    LivetalkChatInputBar("", "고척 스카이돔", isVerified = false, onTextChange = {}, onSendMessage = {})
}
