package com.yagubogu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.yagubogu.ui.login.model.InAppUpdateType
import com.yagubogu.ui.login.model.VersionInfo
import com.yagubogu.ui.main.YaguBoguViewModel
import com.yagubogu.ui.main.model.AutoLoginState
import com.yagubogu.ui.navigation.YaguBoguRoute
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.showToast
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.scope.KoinActivityScope
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class YaguBoguActivity :
    ComponentActivity(),
    AndroidScopeComponent {
    override val scope: Scope by activityScope()

    private val logger = Logger.withTag("YaguBoguActivity")

    private val viewModel: YaguBoguViewModel by viewModel()

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
                viewModel.handleAutoLogin(onAppInitialized = { isAppInitialized = true })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        handleInAppUpdate(onSuccess = {
            viewModel.handleAutoLogin(onAppInitialized = { isAppInitialized = true })
        })
        setContent {
            KoinActivityScope {
                YaguBoguTheme {
                    val autoLoginState: AutoLoginState by viewModel.autoLoginState.collectAsStateWithLifecycle()

                    if (autoLoginState !is AutoLoginState.Loading) {
                        YaguBoguRoute(
                            startRoute =
                                when (autoLoginState) {
                                    AutoLoginState.SignIn -> Route.Main
                                    AutoLoginState.SignUp,
                                    AutoLoginState.Failure,
                                    AutoLoginState.Loading,
                                    -> Route.Login
                                    is AutoLoginState.Maintenance -> Route.Login
                                },
                        )
                    }
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
                val currentVersionCode: Int = BuildKonfig.VERSION_CODE
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
                logger.w { "AppUpdateInfo를 가져오지 못했습니다." }
            }
    }
}
