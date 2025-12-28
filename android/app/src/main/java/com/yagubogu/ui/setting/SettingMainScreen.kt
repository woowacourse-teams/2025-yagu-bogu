package com.yagubogu.ui.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.SettingEventHandler
import com.yagubogu.ui.setting.component.dialog.SettingDialog
import com.yagubogu.ui.setting.component.model.MemberInfoItem
import com.yagubogu.ui.setting.component.model.SettingDialogEvent
import com.yagubogu.ui.setting.component.model.SettingEvent
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun SettingMainScreen(
    onClickSettingAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val context: Context = LocalContext.current
    val scope: CoroutineScope = rememberCoroutineScope()
    val memberInfoItem: State<MemberInfoItem> =
        viewModel.myMemberInfoItem.collectAsStateWithLifecycle(MemberInfoItem())

    val settingEvent: State<SettingEvent?> =
        viewModel.settingEvent.collectAsStateWithLifecycle(null)

    val uCropLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val intent: Intent = result.data ?: return@rememberLauncherForActivityResult
                    val resultUri: Uri =
                        UCrop.getOutput(intent) ?: return@rememberLauncherForActivityResult
                    scope.launch {
                        handleCroppedImage(context, resultUri, viewModel::uploadProfileImage)
                    }
                }

                UCrop.RESULT_ERROR -> {
                    val cropError: Throwable? = result.data?.let { UCrop.getError(it) }
                    Timber.e(cropError, "uCrop Error")
                    context.showToast(context.getString(R.string.setting_edit_profile_image_crop_failed))
                }
            }
        }
    val pickImageLauncher: ManagedActivityResultLauncher<String, Uri?> =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                createUCropIntent(
                    context = context,
                    sourceUri = it,
                    onCropIntentReady = uCropLauncher::launch,
                )
            }
        }

    SettingMainScreen(
        onClickSettingAccount = onClickSettingAccount,
        onNicknameEdit = { viewModel.emitDialogEvent(SettingDialogEvent.NicknameEditDialog) },
        onProfileImageUpload = { pickImageLauncher.launch("image/*") },
        memberInfoItem = memberInfoItem.value,
        appVersion = context.getAppVersion(),
        modifier = modifier,
    )

    SettingDialog(viewModel = viewModel)

    SettingEventHandler(settingEvent = settingEvent.value)
}

@Composable
private fun SettingMainScreen(
    onClickSettingAccount: () -> Unit,
    onNicknameEdit: () -> Unit,
    onProfileImageUpload: () -> Unit,
    memberInfoItem: MemberInfoItem,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(20.dp)
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MyProfile(memberInfoItem = memberInfoItem)

        SettingButtonGroup {
            SettingButton(
                text = stringResource(R.string.setting_edit_profile_image),
                onClick = onProfileImageUpload,
            )
            SettingButton(
                text = stringResource(R.string.setting_edit_nickname),
                onClick = onNicknameEdit,
            )
            SettingButton(text = stringResource(R.string.setting_edit_my_team), onClick = {
                context.startActivity(Intent(context, FavoriteTeamActivity::class.java))
            })
            SettingButton(
                text = stringResource(R.string.setting_manage_account),
                onClick = onClickSettingAccount,
            )
        }

        SettingButtonGroup {
            SettingButton(
                text = stringResource(R.string.setting_notice),
                onClick = { context.openUrl(NOTICE_URL) },
            )
            SettingButton(
                text = stringResource(R.string.setting_contact_us),
                onClick = { context.openUrl(CONTACT_URL) },
            )
            SettingButton(text = stringResource(R.string.setting_open_source_license), onClick = {
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            })
        }

        Text(
            text = stringResource(R.string.setting_app_version, appVersion),
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
        ProfileImage(memberInfoItem.profileImageUrl, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = memberInfoItem.nickName, style = PretendardSemiBold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text =
                stringResource(
                    R.string.setting_main_sign_up_date,
                    memberInfoItem.createdAt.format(DateFormatter.yyyyMMdd),
                ),
            style = PretendardRegular12,
            color = Gray500,
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

private fun Context.getAppVersion(): String =
    try {
        val packageInfo: PackageInfo =
            packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: DEFAULT_VERSION_NAME
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.d("앱 버전 로드 실패 ${e.message}")
        DEFAULT_VERSION_NAME
    }

private fun createUCropIntent(
    context: Context,
    sourceUri: Uri,
    onCropIntentReady: (Intent) -> Unit,
) {
    val fileName = "cropped_image_${System.currentTimeMillis()}"
    val destinationUri = Uri.fromFile(File(context.cacheDir, fileName))

    val options: UCrop.Options =
        UCrop.Options().apply {
            setFreeStyleCropEnabled(false)
            setHideBottomControls(false)
            setCircleDimmedLayer(true)
            setToolbarColor(context.getColor(R.color.gray050))
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(85)
        }

    val uCropIntent: Intent? =
        UCrop
            .of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
            .withOptions(options)
            .getIntent(context)

    uCropIntent?.let { onCropIntentReady(it) }
}

private suspend fun handleCroppedImage(
    context: Context,
    uri: Uri,
    onProfileImageUpload: suspend (Uri, String, Long) -> Result<Unit>,
) {
    runCatching {
        val mimeType: String =
            context
                .contentResolver
                .getType(uri) ?: "image/jpeg"

        val fileSize: Long =
            uri
                .fileSize(context)
                .getOrNull()
                ?: error("파일 사이즈 획득 실패")

        onProfileImageUpload(uri, mimeType, fileSize)
    }.fold(
        onSuccess = { result: Result<Unit> ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                context.showToast(context.getString(R.string.setting_edit_profile_image_upload_failed))
            }
        },
        onFailure = { e: Throwable ->
            if (e is CancellationException) throw e
            Timber.e(e, "프로필 이미지 전처리 실패")
            context.showToast(context.getString(R.string.setting_edit_profile_image_processing_failed))
        },
    )
}

private fun Uri.fileSize(context: Context): Result<Long?> =
    runCatching {
        context.contentResolver
            .query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                    cursor.getLongOrNull(idx)
                } else {
                    null
                }
            }
            ?: context.contentResolver
                .openFileDescriptor(this, "r")
                ?.use { parcelFileDescriptor: ParcelFileDescriptor ->
                    parcelFileDescriptor.statSize.takeIf { it >= 0 }
                }
    }

private fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    startActivity(intent)
}

private const val NOTICE_URL =
    "https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74"
private const val CONTACT_URL = "https://forms.gle/wBhXjfTLyobZa19K8"
private const val DEFAULT_VERSION_NAME = "x.x.x"

@Preview(showBackground = true)
@Composable
private fun SettingMainScreenPreview() {
    SettingMainScreen(
        onClickSettingAccount = {},
        onNicknameEdit = {},
        onProfileImageUpload = {},
        memberInfoItem = MemberInfoItem(nickName = "야구보구"),
        appVersion = "1.0.0",
    )
}
