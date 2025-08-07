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
import com.yagubogu.YaguBoguApplication
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var isAppInitialized: Boolean = false

    private val viewModel: LoginViewModel by viewModels {
        val googleCredentialManager =
            GoogleCredentialManager(this, BuildConfig.WEB_CLIENT_ID, "")
        val app = application as YaguBoguApplication
        LoginViewModelFactory(app.authRepository, googleCredentialManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
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
            val isTokenValid: Boolean = viewModel.isTokenValid()
            if (isTokenValid) {
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
                LoginResult.Success -> {
                    // TODO: 회원가입일 때 팀 선택 화면으로 이동
                    val app = application as YaguBoguApplication
                    lifecycleScope.launch { app.memberRepository.updateFavoriteTeam(Team.LG) }
                    navigateToMain()
                }

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
