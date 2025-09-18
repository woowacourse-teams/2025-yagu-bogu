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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.BuildConfig
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.presentation.MainActivity
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.showSnackbar
import com.yagubogu.presentation.util.showToast
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var isAppInitialized: Boolean = false

    private val viewModel: LoginViewModel by viewModels {
        val googleCredentialManager =
            GoogleCredentialManager(this, BuildConfig.WEB_CLIENT_ID, "")
        val app = application as YaguBoguApplication
        LoginViewModelFactory(
            app.tokenRepository,
            app.authRepository,
            app.memberRepository,
            googleCredentialManager,
        )
    }
    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    private val appUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            if (result.resultCode != RESULT_OK) {
                showToast("결과 받음 ${result.resultCode}")
                Timber.d("Update flow failed! Result code: " + result.resultCode)
                // If the update is canceled or fails,
                // you can request to start the update again.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        checkInAppUpdate()
        handleAutoLogin()
        setupView()
        setupBindings()
    }

    private fun setupSplash() {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isAppInitialized }
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
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkInAppUpdate() {
        val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(this)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        // Checks whether the platform allows the specified type of update,
        // and current version staleness.
        appUpdateInfoTask
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                val availableVersionCode: Int = appUpdateInfo.availableVersionCode()
                val availableVersionInfo = VersionInfo.of(availableVersionCode)
                showToast("$availableVersionCode, $availableVersionInfo")
                Timber.d("성공, 스토어 버전 : $availableVersionCode")

                val currentVersionCode: Int = BuildConfig.VERSION_CODE
                val currentVersionInfo = VersionInfo.of(currentVersionCode)
                Timber.d("현재 버전 : $currentVersionCode")

                val inAppUpdateType = InAppUpdateType.determine(currentVersionInfo, availableVersionInfo)

                when (inAppUpdateType) {
                    InAppUpdateType.IMMEDIATE -> {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            appUpdateResultLauncher,
                            AppUpdateOptions
                                .newBuilder(AppUpdateType.IMMEDIATE)
                                .build(),
                        )
                    }

                    InAppUpdateType.FLEXIBLE -> {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            appUpdateResultLauncher,
                            AppUpdateOptions
                                .newBuilder(AppUpdateType.FLEXIBLE)
                                .build(),
                        )
                    }

                    InAppUpdateType.NONE -> Unit
                }
            }.addOnFailureListener {
                Timber.w("AppUpdateInfo를 가져오지 못했습니다.")
            }
    }
}
