package com.yagubogu.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.common.component.image.ImagePicker
import com.yagubogu.ui.navigation.model.NavigationState
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.toEntries
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.util.UiText
import com.yagubogu.ui.util.slidePopTransition
import com.yagubogu.ui.util.slidePredictivePopTransition
import com.yagubogu.ui.util.slidePushTransition
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_main_title

@Composable
fun SettingScreen(
    navigationState: NavigationState,
    onBackClick: () -> Unit,
    onSettingItemClick: (SettingNavKey) -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccountCancel: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
) {
    var isGalleryOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Gray050,
            topBar = {
                DefaultToolbar(
                    onBackClick = onBackClick,
                    title =
                        stringResource(
                            (navigationState.currentRoute as? SettingNavKey)?.label
                                ?: Res.string.setting_main_title,
                        ),
                )
            },
            modifier = modifier,
        ) { innerPadding: PaddingValues ->
            val entryProvider: (NavKey) -> NavEntry<NavKey> =
                entryProvider {
                    entry<SettingNavKey.SettingMain> {
                        SettingMainScreen(
                            viewModel = viewModel,
                            onSettingAccountClick = { onSettingItemClick(SettingNavKey.SettingAccount) },
                            onFavoriteTeamEditClick = { onFavoriteTeamEditClick() },
                            onProfileImagePickerOpen = { isGalleryOpen = true },
                            onNoticeClick = { onSettingItemClick(SettingNavKey.SettingNotice) },
                            onFaqClick = { onSettingItemClick(SettingNavKey.SettingFaq) },
                            onOssLicenseClick = { onSettingItemClick(SettingNavKey.OssLicense) },
                        )
                    }
                    entry<SettingNavKey.SettingAccount> {
                        SettingAccountScreen(
                            viewModel = viewModel,
                            onDeleteAccountClick = { onSettingItemClick(SettingNavKey.SettingDeleteAccount) },
                            onLogout = onLogout,
                        )
                    }

                    entry<SettingNavKey.SettingNotice> {
                        SettingNoticeScreen()
                    }

                    entry<SettingNavKey.SettingFaq> {
                        SettingFaqScreen()
                    }

                    entry<SettingNavKey.SettingDeleteAccount> {
                        SettingDeleteAccountScreen(
                            viewModel = viewModel,
                            onDeleteAccountCancel = onDeleteAccountCancel,
                            onLogout = onDeleteAccount,
                        )
                    }
                    entry<SettingNavKey.OssLicense> {
                        OssLicenseScreen()
                    }
                }

            NavDisplay(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                entries = navigationState.toEntries(entryProvider),
                onBack = onBackClick,
                transitionSpec = { slidePushTransition() },
                popTransitionSpec = { slidePopTransition() },
                predictivePopTransitionSpec = { edge ->
                    slidePredictivePopTransition(edge)
                },
            )
        }
        if (isGalleryOpen) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Gray900.copy(alpha = 0.3f))
                        .systemBarsPadding()
                        .pointerInput(Unit) { detectTapGestures { } }, // 배경 터치 차단
            ) {
                ImagePicker(
                    onPhotosSelected = { uri: List<String> ->
                        uri.firstOrNull()?.let { viewModel.handleProfileImage(it) }
                    },
                    onError = { message -> viewModel.emitProfileError(UiText.StringRes(message)) },
                    onClosePicker = { isGalleryOpen = false },
                )
            }
        }
    }
}
