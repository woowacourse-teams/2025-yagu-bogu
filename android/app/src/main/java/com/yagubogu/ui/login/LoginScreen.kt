package com.yagubogu.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.login.model.LoginResult
import com.yagubogu.ui.theme.Dimming025
import com.yagubogu.ui.theme.Dimming050
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.EsamanruLight
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LoginScreen(
    googleCredentialManager: GoogleCredentialManager,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier) {
        LoginScreen(
            onGoogleLoginClick = { viewModel.signInWithGoogle(googleCredentialManager) },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    LoginResultHandler(
        snackbarHostState = snackbarHostState,
        loginResultFlow = viewModel.loginResult,
        onSignIn = onSignIn,
        onSignUp = onSignUp,
    )

    BackPressHandler(snackbarHostState, coroutineScope)
}

@Composable
private fun LoginScreen(
    onGoogleLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_login),
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
                text = stringResource(R.string.app_name),
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
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.login_app_description),
                style = EsamanruLight,
                fontSize = 20.dpToSp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.weight(1f))
            LoginButton(
                onClick = onGoogleLoginClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Gray300, RoundedCornerShape(4.dp))
                .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_g_logo),
                contentDescription = stringResource(R.string.login_google_icon_description),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.login_button_google_account),
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
    val loginFailedMessage = stringResource(R.string.login_failed_message)

    LaunchedEffect(Unit) {
        loginResultFlow.collect { loginResult ->
            when (loginResult) {
                is LoginResult.Failure -> {
                    snackbarHostState.showSnackbar(loginFailedMessage)
                    val bundle = bundleOf("reason" to "${loginResult.exception}")
                    Firebase.analytics.logEvent("login_failure", bundle)
                }

                LoginResult.SignIn -> {
                    onSignIn()
                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
                }

                LoginResult.SignUp -> {
                    onSignUp()
                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
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
    LoginScreen(onGoogleLoginClick = {})
}
