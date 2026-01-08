package com.yagubogu.ui.login

import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yagubogu.R
import com.yagubogu.presentation.login.LoginActivity
import com.yagubogu.presentation.login.LoginViewModel
import com.yagubogu.presentation.login.auth.GoogleCredentialManager
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.EsamanruLight
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val googleCredentialManager: GoogleCredentialManager =
        (LocalActivity.current as LoginActivity).googleCredentialManager

    LoginScreen(
        onGoogleLoginClick = { viewModel.signInWithGoogle(googleCredentialManager) },
        modifier = modifier,
    )
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
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 40.dp),
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = stringResource(R.string.app_name),
                style =
                    EsamanruBold.copy(
                        shadow =
                            Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
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
            LoginButton(onClick = onGoogleLoginClick)
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun LoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Gray300, RoundedCornerShape(4.dp))
                .noRippleClickable(onClick),
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

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(onGoogleLoginClick = {})
}
