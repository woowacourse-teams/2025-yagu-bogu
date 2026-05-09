package com.yagubogu.ui.onboarding.nickname

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.onboarding.nickname.component.NicknameInputField
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.img_baseball_animal_neko
import yagubogu.composeapp.generated.resources.nickname_next
import yagubogu.composeapp.generated.resources.nickname_start_default
import yagubogu.composeapp.generated.resources.nickname_subtitle
import yagubogu.composeapp.generated.resources.nickname_title

@Composable
fun NicknameScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NicknameViewModel = koinViewModel(),
) {
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val isDuplicateChecked by viewModel.isDuplicateChecked.collectAsStateWithLifecycle()
    val nicknameError by viewModel.nicknameError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToMainEvent.collect {
            onCompleted()
        }
    }

    NicknameScreenContent(
        nickname = nickname,
        nicknameError = nicknameError,
        isDuplicateChecked = isDuplicateChecked,
        onNicknameChange = viewModel::onNicknameChanged,
        onCheckDuplicate = viewModel::updateNickname,
        onDefaultNicknameClick = viewModel::useDefaultNickname,
        onNextClick = viewModel::onNextClick,
        modifier = modifier,
    )
}

@Composable
private fun NicknameScreenContent(
    nickname: String,
    nicknameError: String?,
    isDuplicateChecked: Boolean,
    onNicknameChange: (String) -> Unit,
    onCheckDuplicate: () -> Unit,
    onDefaultNicknameClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = White,
        modifier =
            modifier.fillMaxSize().noRippleClickable {
                focusManager.clearFocus()
            },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .noRippleClickable { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = stringResource(Res.string.nickname_title),
                style = EsamanruMedium,
                fontSize = 32.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.nickname_subtitle),
                style = PretendardMedium16,
                color = Gray400,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(48.dp))

            NicknameInputField(
                nickname = nickname,
                onNicknameChange = onNicknameChange,
                onCheckDuplicate = {
                    onCheckDuplicate()
                    focusManager.clearFocus()
                },
                isDuplicateChecked = isDuplicateChecked,
                nicknameError = nicknameError,
            )

            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.img_baseball_animal_neko),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(Res.string.nickname_start_default),
                style = PretendardSemiBold16,
                color = Gray400,
                textDecoration = TextDecoration.Underline,
                modifier =
                    Modifier
                        .noRippleClickable { onDefaultNicknameClick() }
                        .padding(vertical = 16.dp),
            )

            Button(
                onClick = onNextClick,
                enabled = isDuplicateChecked && nickname.isNotEmpty(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 8.dp),
                shape = RoundedCornerShape(30.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Primary500,
                        contentColor = White,
                        disabledContainerColor = Gray100,
                        disabledContentColor = Gray400,
                    ),
            ) {
                Text(
                    stringResource(Res.string.nickname_next),
                    style = EsamanruMedium,
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview
@Composable
private fun NicknameScreenPreview() {
    NicknameScreenContent(
        nickname = "귀염뽀짝순둥애교보육",
        nicknameError = "중복된 닉네임이 존재합니다.",
        isDuplicateChecked = false,
        onNicknameChange = {},
        onCheckDuplicate = {},
        onDefaultNicknameClick = {},
        onNextClick = {},
    )
}

@Preview
@Composable
private fun NicknameScreenPassPreview() {
    NicknameScreenContent(
        nickname = "귀염뽀짝순둥애교보육",
        nicknameError = null,
        isDuplicateChecked = true,
        onNicknameChange = {},
        onCheckDuplicate = {},
        onDefaultNicknameClick = {},
        onNextClick = {},
    )
}
