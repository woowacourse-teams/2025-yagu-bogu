package com.yagubogu.presentation.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.BuildConfig
import com.yagubogu.R
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.data.repository.AuthDefaultRepository
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.presentation.MainActivity
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
        val authRepository = AuthDefaultRepository(googleCredentialManager)
        LoginViewModelFactory(authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        setupView()
        setupBindings()
        performInitialization()
    }

    private fun setupSplash() {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isAppInitialized }
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

    private fun performInitialization() {
        lifecycleScope.launch {
            try {
                // Todo : 초기화 작업 수행, (LoginViewModel에서 초기 Api 요청, 데이터베이스 조회 등)
            } catch (e: Exception) {
                Timber.e(e, "초기화 실패")
            } finally {
                isAppInitialized = true
            }
        }
    }

    private fun setupBindings() {
        binding.viewModel = viewModel

        viewModel.loginResult.observe(this) { value: LoginResult ->
            when (value) {
                is LoginResult.Success -> navigateToMain()
                is LoginResult.Failure -> showSnackbar(R.string.login_failed_message)
                LoginResult.Cancel -> Unit
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSnackbar(
        @StringRes message: Int,
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(Color.DKGRAY)
            setTextColor(context.getColor(R.color.white))
            show()
        }
    }
}
