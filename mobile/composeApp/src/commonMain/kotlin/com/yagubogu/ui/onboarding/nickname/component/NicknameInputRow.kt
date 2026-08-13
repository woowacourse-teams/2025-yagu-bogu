package com.yagubogu.ui.onboarding.nickname.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Rose
import com.yagubogu.ui.theme.White
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.nickname_available
import yagubogu.composeapp.generated.resources.nickname_check_duplicate
import yagubogu.composeapp.generated.resources.nickname_placeholder

@Composable
fun NicknameInputField(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onCheckDuplicate: () -> Unit,
    isDuplicateChecked: Boolean,
    nicknameError: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .background(White, RoundedCornerShape(12.dp))
                        .border(1.dp, Gray300, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (nickname.isEmpty()) {
                    Text(
                        stringResource(Res.string.nickname_placeholder),
                        color = Gray400,
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = nickname,
                    onValueChange = { if (it.length <= 15) onNicknameChange(it) },
                    textStyle = PretendardSemiBold16,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            Button(
                onClick = onCheckDuplicate,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Primary500,
                        contentColor = White,
                        disabledContainerColor = Gray400,
                        disabledContentColor = White,
                    ),
                contentPadding = PaddingValues(horizontal = 16.dp),
                enabled = nickname.isNotEmpty() && !isDuplicateChecked,
            ) {
                Text(stringResource(Res.string.nickname_check_duplicate), style = PretendardSemiBold16)
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(start = 4.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            when {
                nicknameError != null -> {
                    Text(text = nicknameError, color = Rose, style = PretendardSemiBold, fontSize = 14.sp)
                }
                isDuplicateChecked -> {
                    Text(
                        text = stringResource(Res.string.nickname_available),
                        color = Primary500,
                        style = PretendardSemiBold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "초기 상태 (입력 전)")
@Composable
private fun NicknameInputFieldEmptyPreview() {
    Box(modifier = Modifier.padding(16.dp).background(White)) {
        NicknameInputField(
            nickname = "",
            onNicknameChange = {},
            onCheckDuplicate = {},
            isDuplicateChecked = false,
            nicknameError = null,
        )
    }
}

@Preview(showBackground = true, name = "입력 중 (중복 확인 전)")
@Composable
private fun NicknameInputFieldTypingPreview() {
    Box(modifier = Modifier.padding(16.dp).background(White)) {
        NicknameInputField(
            nickname = "문동주",
            onNicknameChange = {},
            onCheckDuplicate = {},
            isDuplicateChecked = false,
            nicknameError = null,
        )
    }
}

@Preview(showBackground = true, name = "중복 닉네임")
@Composable
private fun NicknameInputFieldErrorPreview() {
    Box(modifier = Modifier.padding(16.dp).background(White)) {
        NicknameInputField(
            nickname = "문동주",
            onNicknameChange = {},
            onCheckDuplicate = {},
            isDuplicateChecked = false,
            nicknameError = "이미 사용 중인 닉네임입니다.",
        )
    }
}

@Preview(showBackground = true, name = "사용 가능 닉네임")
@Composable
private fun NicknameInputFieldSuccessPreview() {
    Box(modifier = Modifier.padding(16.dp).background(White)) {
        NicknameInputField(
            nickname = "레전드닉네임",
            onNicknameChange = {},
            onCheckDuplicate = {},
            isDuplicateChecked = true,
            nicknameError = null,
        )
    }
}
