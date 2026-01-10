package com.yagubogu.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.yagubogu.BuildConfig
import com.yagubogu.R
import com.yagubogu.presentation.login.LoginViewModel
import com.yagubogu.presentation.login.auth.GoogleCredentialManager
import com.yagubogu.presentation.login.model.InAppUpdateType
import com.yagubogu.presentation.login.model.VersionInfo
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.navigation.NavigationRoot
import com.yagubogu.ui.navigation.Route
import com.yagubogu.ui.theme.YaguBoguTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var googleCredentialManager: GoogleCredentialManager

    private var shouldImmediateUpdate: Boolean = true
    private var isAppInitialized: Boolean = false

    // 인앱 업데이트 요청 후 결과를 처리하기 위한 ActivityResultLauncher
    // - StartIntentSenderForResult() : 인앱 업데이트 플로우 실행 후 결과를 콜백으로 받음
    // - shouldImmediateUpdate = true 인 경우, 업데이트를 완료하지 않으면 앱을 종료하도록 처리
    private val appUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (shouldImmediateUpdate && result.resultCode != RESULT_OK) {
                showToast(R.string.login_should_immediate_update_message, true)
                finish()
            } else if (!shouldImmediateUpdate) {
                loginViewModel.handleAutoLogin(onAppInitialized = { isAppInitialized = true })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        handleInAppUpdate(onSuccess = {
            loginViewModel.handleAutoLogin(onAppInitialized = { isAppInitialized = true })
        })
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        Timber.d("$isAppInitialized")
        setContent {
            YaguBoguTheme {
                val canAutoLogin: Boolean? by loginViewModel.canAutoLogin.collectAsStateWithLifecycle()

                canAutoLogin?.let { canAutoLogin ->
                    NavigationRoot(
                        googleCredentialManager = googleCredentialManager,
                        startRoute = if (canAutoLogin) Route.BottomRoute else Route.LoginRoute,
                    )
                }
            }
        }
    }

    private fun setupSplash() {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { shouldImmediateUpdate || !isAppInitialized }
    }

    private fun handleInAppUpdate(onSuccess: () -> Unit) {
        val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                // 이미 업데이트가 다운로드 완료된 상태라면 설치를 완료하도록 요청
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                    showToast(getString(R.string.login_complete_update_message), true)
                }

                when (appUpdateInfo.updateAvailability()) {
                    // 강제 업데이트 중 실패했을 때 재개
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            appUpdateResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        )
                        return@addOnSuccessListener
                    }

                    // 업데이트 할 수 없거나, 네트워크 등 오류 발생했을 때 splash 종료
                    UpdateAvailability.UNKNOWN, UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        shouldImmediateUpdate = false
                        onSuccess()
                        return@addOnSuccessListener
                    }

                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        Unit
                    }
                }

                // 스토어에서 제공되는 최신 앱 버전 코드
                val availableVersionCode: Int = appUpdateInfo.availableVersionCode()
                val availableVersionInfo = VersionInfo.of(availableVersionCode)

                // 현재 앱의 버전 코드
                val currentVersionCode: Int = BuildConfig.VERSION_CODE
                val currentVersionInfo = VersionInfo.of(currentVersionCode)

                // 현재 버전과 최신 버전을 비교해 업데이트 타입 결정
                val inAppUpdateType =
                    InAppUpdateType.determine(currentVersionInfo, availableVersionInfo)

                when (inAppUpdateType) {
                    // 강제 업데이트가 필요한 경우
                    InAppUpdateType.IMMEDIATE -> {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                appUpdateResultLauncher,
                                AppUpdateOptions
                                    .newBuilder(AppUpdateType.IMMEDIATE)
                                    .build(),
                            )
                        } else {
                            shouldImmediateUpdate = false
                            onSuccess()
                        }
                    }

                    // 권장 업데이트 (사용자가 원할 때 업데이트 가능)
                    InAppUpdateType.FLEXIBLE -> {
                        shouldImmediateUpdate = false

                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                appUpdateResultLauncher,
                                AppUpdateOptions
                                    .newBuilder(AppUpdateType.FLEXIBLE)
                                    .build(),
                            )
                        }
                    }

                    InAppUpdateType.NONE -> {
                        shouldImmediateUpdate = false
                        onSuccess()
                    }
                }
            }.addOnFailureListener {
                shouldImmediateUpdate = false
                onSuccess()
                Timber.w("AppUpdateInfo를 가져오지 못했습니다.")
            }
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }
}
