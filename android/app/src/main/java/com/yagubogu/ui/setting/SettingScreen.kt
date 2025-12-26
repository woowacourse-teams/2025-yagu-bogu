package com.yagubogu.ui.setting

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.crashlytics.internal.common.IdManager.DEFAULT_VERSION_NAME
import com.yagubogu.R
import com.yagubogu.presentation.setting.MemberInfoItem
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import timber.log.Timber
import java.time.LocalDate

@Composable
fun SettingScreen(
    memberInfoItem: MemberInfoItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(20.dp)
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MyProfile(memberInfoItem = memberInfoItem)
        ButtonGroup {
            SettingButton(text = stringResource(R.string.setting_edit_profile_image), onClick = {})
            SettingButton(text = stringResource(R.string.setting_edit_nickname), onClick = {})
            SettingButton(text = stringResource(R.string.setting_edit_my_team), onClick = {})
            SettingButton(text = stringResource(R.string.setting_manage_account), onClick = {})
        }
        ButtonGroup {
            SettingButton(text = stringResource(R.string.setting_notice), onClick = {})
            SettingButton(text = stringResource(R.string.setting_contact_us), onClick = {})
            SettingButton(text = stringResource(R.string.setting_open_source_license), onClick = {})
        }
        Text(
            text = stringResource(R.string.setting_app_version, getAppVersion()),
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
        )
    }
}

@Composable
private fun MyProfile(
    memberInfoItem: MemberInfoItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        ProfileImage(memberInfoItem.profileImageUrl, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = memberInfoItem.nickName, style = PretendardSemiBold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.setting_main_sign_up_date, memberInfoItem.createdAt),
            style = PretendardRegular12,
            color = Gray500,
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ButtonGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            content()
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SettingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Spacer(
            modifier =
                Modifier
                    .height(10.dp)
                    .fillMaxWidth(),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = text, style = PretendardSemiBold16)
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Gray500,
            )
        }
        Spacer(
            modifier =
                Modifier
                    .height(10.dp)
                    .fillMaxWidth(),
        )
    }
}

@Composable
private fun getAppVersion(): String {
    val context = LocalContext.current
    return try {
        val packageInfo: PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: DEFAULT_VERSION_NAME
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.d("앱 버전 로드 실패 ${e.message}")
        DEFAULT_VERSION_NAME
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    SettingScreen(
        memberInfoItem =
            MemberInfoItem(
                nickName = "귀여운보욱이",
                createdAt = LocalDate.now(),
                favoriteTeam = "KIA",
                profileImageUrl = "",
            ),
    )
}
