package com.yagubogu.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.BuildConfig
import com.yagubogu.R
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.presentation.MainActivity
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.showSnackbar
import com.yagubogu.presentation.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: LoginViewModel.Factory

    @Inject
    lateinit var googleCredentialManager: GoogleCredentialManager

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.provideFactory(viewModelFactory, googleCredentialManager)
    }

    private var shouldImmediateUpdate: Boolean = true
    private var isAppInitialized: Boolean = false

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    // 인앱 업데이트 요청 후 결과를 처리하기 위한 ActivityResultLauncher
    // - StartIntentSenderForResult() : 인앱 업데이트 플로우 실행 후 결과를 콜백으로 받음
    // - shouldImmediateUpdate = true 인 경우, 업데이트를 완료하지 않으면 앱을 종료하도록 처리
    private val appUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (shouldImmediateUpdate && result.resultCode != RESULT_OK) {
                showToast(getString(R.string.login_should_immediate_update_message), true)
                finish()
            } else if (!shouldImmediateUpdate) {
                handleAutoLogin()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        handleInAppUpdate(onSuccess = { handleAutoLogin() })
        setupView()
        setupBindings()
    }

    private fun setupSplash() {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { shouldImmediateUpdate || !isAppInitialized }
    }

    private fun handleAutoLogin() {
        lifecycleScope.launch {
            if (!viewModel.isTokenValid()) {
                isAppInitialized = true
                return@launch
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)

            if (viewModel.isNewUser()) {
                navigateToFavoriteTeam()
            } else {
                navigateToMain()
            }
            isAppInitialized = true
        }
    }

    private fun setupView() {
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = true
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.constraintActivityLoginRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
    }

    private fun setupBindings() {
        binding.viewModel = viewModel

        viewModel.loginResult.observe(this) { value: LoginResult ->
            when (value) {
                LoginResult.SignUp -> {
                    navigateToFavoriteTeam()
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
                }

                LoginResult.SignIn -> {
                    navigateToMain()
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
                }

                is LoginResult.Failure -> {
                    binding.root.showSnackbar(R.string.login_failed_message)
                    val bundle = bundleOf("reason" to "${value.exception}")
                    firebaseAnalytics.logEvent("login_failure", bundle)
                }

                LoginResult.Cancel -> Unit
            }
        }
    }

    private fun navigateToFavoriteTeam() {
        startActivity(Intent(this, FavoriteTeamActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
        finish()
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

                    UpdateAvailability.UPDATE_AVAILABLE -> Unit
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
}
