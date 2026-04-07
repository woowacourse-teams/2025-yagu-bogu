package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.BuildKonfig
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.login.model.VersionInfo
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.SettingToggleButton
import com.yagubogu.ui.setting.component.dialog.NicknameEditDialog
import com.yagubogu.ui.setting.model.MemberInfoItem
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.showSingleSnackbar
import com.yagubogu.ui.util.yyyyMMddFormatter
import kotlinx.datetime.format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_app_version
import yagubogu.composeapp.generated.resources.setting_contact_us
import yagubogu.composeapp.generated.resources.setting_edit_my_team
import yagubogu.composeapp.generated.resources.setting_edit_nickname
import yagubogu.composeapp.generated.resources.setting_edit_profile_image
import yagubogu.composeapp.generated.resources.setting_edited_nickname_alert
import yagubogu.composeapp.generated.resources.setting_geofencing_notification
import yagubogu.composeapp.generated.resources.setting_geofencing_notification_tooltip
import yagubogu.composeapp.generated.resources.setting_main_sign_up_date
import yagubogu.composeapp.generated.resources.setting_manage_account
import yagubogu.composeapp.generated.resources.setting_notice
import yagubogu.composeapp.generated.resources.setting_open_source_license

@Composable
fun SettingMainScreen(
    viewModel: SettingViewModel,
    onSettingAccountClick: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onProfileImagePickerOpen: () -> Unit,
    onOssLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = LocalSnackbarHostState.current

    val memberInfoItem: State<MemberInfoItem> =
        viewModel.myMemberInfoItem.collectAsStateWithLifecycle(MemberInfoItem())

    var showNicknameEditDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val geofenceNotification: State<Boolean> = viewModel.geofenceNotification.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMemberInfo()
    }

    LaunchedEffect(Unit) {
        viewModel.settingEvent.collect { settingEvent: SettingEvent ->
            when (settingEvent) {
                is SettingEvent.NicknameEditSuccess -> {
                    val message =
                        getString(
                            Res.string.setting_edited_nickname_alert,
                            settingEvent.newNickname,
                        )
                    snackbarHostState.showSingleSnackbar(
                        scope = this,
                        message = message,
                    )
                }

                is SettingEvent.NicknameEditFailure -> {
                    val errorMessage = settingEvent.uiText.asString()
                    snackbarHostState.showSingleSnackbar(
                        scope = this,
                        message = errorMessage,
                    )
                }

                else -> Unit
            }
        }
    }

    SettingMainScreen(
        onClickSettingAccount = onSettingAccountClick,
        onFavoriteTeamEditClick = onFavoriteTeamEditClick,
        onNicknameEdit = { showNicknameEditDialog = true },
        onProfileImageUpload = onProfileImagePickerOpen,
        onOssLicenseClick = onOssLicenseClick,
        memberInfoItem = memberInfoItem.value,
        appVersion = getAppVersion(),
        geofenceNotification = geofenceNotification.value,
        updateGeofenceNotification = viewModel::updateGeofenceNotification,
        modifier = modifier,
    )

    if (showNicknameEditDialog) {
        NicknameEditDialog(
            nickname =
                viewModel.myMemberInfoItem
                    .collectAsState()
                    .value.nickName,
            onConfirm = { nickname ->
                viewModel.updateNickname(nickname)
                showNicknameEditDialog = false
            },
            onCancel = { showNicknameEditDialog = false },
        )
    }
}

@Composable
private fun SettingMainScreen(
    onClickSettingAccount: () -> Unit,
    onNicknameEdit: () -> Unit,
    onProfileImageUpload: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onOssLicenseClick: () -> Unit,
    memberInfoItem: MemberInfoItem,
    appVersion: String,
    geofenceNotification: Boolean,
    updateGeofenceNotification: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MyProfile(memberInfoItem = memberInfoItem)

        SettingButtonGroup {
            SettingToggleButton(
                text = stringResource(Res.string.setting_geofencing_notification),
                toolTipText = stringResource(Res.string.setting_geofencing_notification_tooltip),
                checked = geofenceNotification,
                onCheckedChange = { updateGeofenceNotification(!geofenceNotification) },
            )
            SettingButton(
                text = stringResource(Res.string.setting_edit_profile_image),
                onClick = onProfileImageUpload,
            )
            SettingButton(
                text = stringResource(Res.string.setting_edit_nickname),
                onClick = onNicknameEdit,
            )
            SettingButton(
                text = stringResource(Res.string.setting_edit_my_team),
                onClick = onFavoriteTeamEditClick,
            )
            SettingButton(
                text = stringResource(Res.string.setting_manage_account),
                onClick = onClickSettingAccount,
            )
        }

        SettingButtonGroup {
            SettingButton(
                text = stringResource(Res.string.setting_notice),
                onClick = { uriHandler.openUri(NOTICE_URL) },
            )
            SettingButton(
                text = stringResource(Res.string.setting_contact_us),
                onClick = { uriHandler.openUri(CONTACT_URL) },
            )
            SettingButton(
                text = stringResource(Res.string.setting_open_source_license),
                onClick = onOssLicenseClick,
            )
        }

        Text(
            text = stringResource(Res.string.setting_app_version, appVersion),
            textAlign = TextAlign.Center,
            style = PretendardMedium12,
            color = Gray400,
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
        ProfileImage(memberInfoItem.profileImageUrl, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = memberInfoItem.nickName, style = PretendardSemiBold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text =
                stringResource(
                    Res.string.setting_main_sign_up_date,
                    memberInfoItem.createdAt.format(yyyyMMddFormatter),
                ),
            style = PretendardRegular12,
            color = Gray500,
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * ImagePickerKMP로 자른 이미지를
 * 백엔드에서 요구하는 프로파일 이미지 규격(jpeg, 5mb)으로 컨버팅하여 업로드합니다.
 */
expect suspend fun handleImagePickerKMPCroppedImage(
    onUploadFailure: () -> Unit,
    onProcessingFailure: () -> Unit,
    sourceImageUri: String,
    onProfileImageUpload: suspend (String, String, Long) -> Result<Unit>,
)

private fun getAppVersion(): String {
    val appVersion = BuildKonfig.VERSION_CODE
    val isDebug: Boolean = BuildKonfig.IS_DEBUG
    val availableVersionInfo = VersionInfo.of(appVersion)
    val versionName =
        availableVersionInfo.major.toString() + "." + availableVersionInfo.minor.toString() + "." + availableVersionInfo.patch.toString()

    return when (isDebug) {
        true -> "$versionName.debug"
        false -> versionName
    }
}

private const val NOTICE_URL =
    "https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74"
private const val CONTACT_URL = "https://forms.gle/wBhXjfTLyobZa19K8"

@Preview(showBackground = true)
@Composable
private fun SettingMainScreenPreview() {
    SettingMainScreen(
        onClickSettingAccount = {},
        onNicknameEdit = {},
        onProfileImageUpload = {},
        onFavoriteTeamEditClick = {},
        onOssLicenseClick = {},
        memberInfoItem = MemberInfoItem(nickName = "야구보구"),
        appVersion = "1.0.0",
        geofenceNotification = false,
        updateGeofenceNotification = {},
    )
}
