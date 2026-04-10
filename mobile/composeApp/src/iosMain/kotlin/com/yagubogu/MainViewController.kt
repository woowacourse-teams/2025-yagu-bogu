package com.yagubogu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.main.YaguBoguViewModel
import com.yagubogu.ui.main.model.AutoLoginState
import com.yagubogu.ui.navigation.YaguBoguRoute
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.theme.YaguBoguTheme
import org.koin.compose.viewmodel.koinViewModel

@Suppress("FunctionName")
fun MainViewController() =
    ComposeUIViewController {
        YaguBoguTheme {
            YaguBoguIosApp()
        }
    }

@Composable
private fun YaguBoguIosApp() {
    val viewModel: YaguBoguViewModel = koinViewModel()
    val autoLoginState: AutoLoginState by viewModel.autoLoginState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleAutoLogin(onAppInitialized = {})
    }

    YaguBoguTheme {
        if (autoLoginState !is AutoLoginState.Loading) {
            YaguBoguRoute(
                startRoute =
                    when (autoLoginState) {
                        AutoLoginState.SignIn -> Route.Main
                        AutoLoginState.SignUp,
                        AutoLoginState.Failure,
                        AutoLoginState.Loading,
                        -> Route.Login
                    },
            )
        }
    }
}
