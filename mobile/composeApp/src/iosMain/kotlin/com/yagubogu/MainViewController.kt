package com.yagubogu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.di.alarmeeModule
import com.yagubogu.di.authModule
import com.yagubogu.di.commonLocalModule
import com.yagubogu.di.commonModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.geofenceModule
import com.yagubogu.di.localModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import com.yagubogu.di.viewModelModule
import com.yagubogu.ui.login.AppleSignInDelegate
import com.yagubogu.ui.login.GoogleSignInDelegate
import com.yagubogu.ui.main.YaguBoguViewModel
import com.yagubogu.ui.main.model.AutoLoginState
import com.yagubogu.ui.navigation.YaguBoguRoute
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.theme.YaguBoguTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@Suppress("FunctionName")
fun MainViewController(
    googleSignInDelegate: GoogleSignInDelegate,
    appleSignInDelegate: AppleSignInDelegate,
) = ComposeUIViewController {
    KoinApplication(
        configuration =
            koinConfiguration(
                declaration = {
                    modules(
                        module {
                            single<GoogleSignInDelegate> { googleSignInDelegate }
                            single<AppleSignInDelegate> { appleSignInDelegate }
                        },
                        authModule,
                        commonModule,
                        datasourceModule,
                        localModule,
                        commonLocalModule,
                        geofenceModule,
                        alarmeeModule,
                        networkModule,
                        repositoryModule,
                        serviceModule,
                        timeModule,
                        viewModelModule,
                    )
                },
            ),
        content = {
            YaguBoguIosApp()
        },
    )
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
