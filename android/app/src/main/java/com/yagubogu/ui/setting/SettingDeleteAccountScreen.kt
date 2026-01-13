package com.yagubogu.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.ui.setting.component.SettingEventHandler
import com.yagubogu.ui.setting.component.dialog.DeleteAccountDialog
import com.yagubogu.ui.setting.model.MemberInfoItem
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White

@Composable
fun SettingDeleteAccountScreen(
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val memberInfoItem: State<MemberInfoItem> =
        viewModel.myMemberInfoItem.collectAsStateWithLifecycle()
    val settingEvent: State<SettingEvent?> =
        viewModel.settingEvent.collectAsStateWithLifecycle(null)

    var showDeleteAccountDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    SettingDeleteAccountScreen(
        onCancelDeleteAccount = {
            viewModel.cancelDeleteAccount()
            showDeleteAccountDialog = false
        },
        onConfirmDeleteAccount = { showDeleteAccountDialog = true },
        memberInfoItem = memberInfoItem.value,
        modifier = modifier,
    )

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = {
                viewModel.deleteAccount()
                showDeleteAccountDialog = false
            },
            onCancel = { showDeleteAccountDialog = false },
        )
    }

    SettingEventHandler(
        settingEvent = settingEvent.value,
        navigateToHome = navigateToHome,
    )
}

@Composable
private fun SettingDeleteAccountScreen(
    onCancelDeleteAccount: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    memberInfoItem: MemberInfoItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(horizontal = 30.dp, vertical = 40.dp)
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        DeleteAccountQuestion()
        RememberImageMessage(memberInfoItem)
        DeleteAccountButtons(
            onCancel = onCancelDeleteAccount,
            onConfirm = onConfirmDeleteAccount,
        )
    }
}

@Composable
private fun DeleteAccountQuestion(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.setting_delete_account_question_title),
            style = PretendardBold,
            fontSize = 24.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.setting_delete_account_question_message),
            style = PretendardRegular,
            fontSize = 14.sp,
            color = Gray400,
        )
    }
}

@Composable
private fun RememberImageMessage(
    memberInfoItem: MemberInfoItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Image(
            painterResource(R.drawable.img_baseball_leave_stadium),
            contentDescription = stringResource(R.string.setting_delete_account_illustration_description),
            modifier =
                Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text =
                stringResource(
                    R.string.setting_remember_message,
                    memberInfoItem.nickName,
                    memberInfoItem.memberPeriod,
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = PretendardMedium,
            fontSize = 14.sp,
            color = Gray400,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun DeleteAccountButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(
            onClick = {
                onCancel()
                Firebase.analytics.logEvent("delete_account_cancel", null)
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Primary500,
                    contentColor = White,
                ),
            contentPadding = PaddingValues(16.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(R.string.setting_delete_account_cancel),
                style = PretendardBold16,
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                onConfirm()
                Firebase.analytics.logEvent("delete_account_confirm", null)
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Gray100,
                    contentColor = Gray400,
                ),
            contentPadding = PaddingValues(12.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(R.string.setting_delete_account),
                style = PretendardBold16,
            )
        }
    }
}

@Preview
@Composable
private fun SettingDeleteAccountScreenPreview() {
    SettingDeleteAccountScreen(
        onCancelDeleteAccount = {},
        onConfirmDeleteAccount = {},
        memberInfoItem = MemberInfoItem(),
    )
}
