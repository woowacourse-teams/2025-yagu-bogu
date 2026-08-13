package com.yagubogu.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.di.Qualifier
import com.yagubogu.ui.common.platform.PlatformType
import com.yagubogu.ui.common.platform.currentPlatform
import com.yagubogu.ui.login.auth.OAuthCredentialManager
import com.yagubogu.ui.login.model.LoginResult
import com.yagubogu.ui.login.model.OAuthProvider
import com.yagubogu.ui.login.model.PopupNoticeDialog
import com.yagubogu.ui.theme.Dimming025
import com.yagubogu.ui.theme.Dimming050
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.EsamanruLight
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.app_name
import yagubogu.composeapp.generated.resources.ic_apple_logo
import yagubogu.composeapp.generated.resources.ic_google_g_logo
import yagubogu.composeapp.generated.resources.img_login
import yagubogu.composeapp.generated.resources.login_app_description
import yagubogu.composeapp.generated.resources.login_apple_icon_description
import yagubogu.composeapp.generated.resources.login_button_apple_account
import yagubogu.composeapp.generated.resources.login_button_google_account
import yagubogu.composeapp.generated.resources.login_failed_message
import yagubogu.composeapp.generated.resources.login_google_icon_description

@Composable
fun LoginScreen(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    googleCredentialManager: OAuthCredentialManager = koinInject(named<Qualifier.Google>()),
    appleCredentialManager: OAuthCredentialManager = koinInject(named<Qualifier.Apple>()),
    viewModel: LoginViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val maintenanceInfo by viewModel.maintenanceInfo.collectAsStateWithLifecycle()
    val isMaintenanceConfirm = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        LoginScreen(
            onGoogleLoginClick = { viewModel.signInWithGoogle(googleCredentialManager) },
            onAppleLoginClick = { viewModel.signInWithApple(appleCredentialManager) },
            isLoginBlock = maintenanceInfo?.isLoginBlock ?: true,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        maintenanceInfo?.let { info ->
            if (info.shouldShowPopup && !isMaintenanceConfirm.value) {
                PopupNoticeDialog(
                    onConfirm = { isChecked ->
                        isMaintenanceConfirm.value = true
                        if (isChecked) {
                            viewModel.ignoreMaintenanceDialog(info.id, info.skippableDays ?: 0)
                        }
                    },
                    popupNoticeInfo = info,
                    modifier = Modifier,
                )
            }
        }
    }
    LoginResultHandler(
        snackbarHostState = snackbarHostState,
        loginResultFlow = viewModel.loginResult,
        onSignIn = onSignIn,
        onSignUp = onSignUp,
    )

    BackPressHandler()
}

@Composable
private fun LoginScreen(
    onGoogleLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    isLoginBlock: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.img_login),
            contentDescription = null,
            modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Dimming050)
                    .padding(horizontal = 40.dp),
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = stringResource(Res.string.app_name),
                style =
                    EsamanruBold.copy(
                        shadow =
                            Shadow(
                                color = Dimming025,
                                offset = Offset(x = 2f, y = 12f),
                                blurRadius = 8f,
                            ),
                    ),
                fontSize = 56.dpToSp,
                color = White,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(Res.string.login_app_description),
                style = EsamanruLight,
                fontSize = 20.dpToSp,
                color = White,
            )
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (!isLoginBlock) {
                    LoginButton(
                        provider = OAuthProvider.GOOGLE,
                        onClick = onGoogleLoginClick,
                    )

                    if (currentPlatform == PlatformType.IOS) {
                        LoginButton(
                            provider = OAuthProvider.APPLE,
                            onClick = onAppleLoginClick,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun LoginButton(
    provider: OAuthProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, CircleShape)
                .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter =
                    painterResource(
                        when (provider) {
                            OAuthProvider.GOOGLE -> Res.drawable.ic_google_g_logo
                            OAuthProvider.APPLE -> Res.drawable.ic_apple_logo
                        },
                    ),
                contentDescription =
                    stringResource(
                        when (provider) {
                            OAuthProvider.GOOGLE -> Res.string.login_google_icon_description
                            OAuthProvider.APPLE -> Res.string.login_apple_icon_description
                        },
                    ),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text =
                    stringResource(
                        when (provider) {
                            OAuthProvider.GOOGLE -> Res.string.login_button_google_account
                            OAuthProvider.APPLE -> Res.string.login_button_apple_account
                        },
                    ),
                style = PretendardSemiBold,
                fontSize = 18.dpToSp,
            )
        }
    }
}

@Composable
fun LoginResultHandler(
    snackbarHostState: SnackbarHostState,
    loginResultFlow: SharedFlow<LoginResult>,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    val loginFailedMessage = stringResource(Res.string.login_failed_message)

    LaunchedEffect(Unit) {
        loginResultFlow.collect { loginResult ->
            when (loginResult) {
                is LoginResult.Failure -> {
                    Logger.withTag("LoginResult").d { loginResult.toString() }
                    snackbarHostState.showSnackbar(loginFailedMessage)
                    AnalyticsLogger.logEvent(
                        "login_failure",
                        mapOf("reason" to "${loginResult.exception}"),
                    )
                }

                LoginResult.SignIn -> {
                    onSignIn()
                    AnalyticsLogger.logEvent("login")
                }

                LoginResult.SignUp -> {
                    onSignUp()
                    AnalyticsLogger.logEvent("login")
                }

                LoginResult.Cancel -> {
                    Unit
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(onGoogleLoginClick = {}, onAppleLoginClick = {}, isLoginBlock = false)
}
