package com.yagubogu.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.yagubogu.BuildConfig
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.presentation.MainActivity
import com.yagubogu.presentation.favorite.FavoriteTeamActivity
import com.yagubogu.presentation.util.showSnackbar
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
        LoginViewModelFactory(
            app.tokenRepository,
            app.authRepository,
            app.memberRepository,
            googleCredentialManager,
        )
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
            if (!viewModel.isTokenValid()) {
                isAppInitialized = true
                return@launch
            }

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
                LoginResult.SignUp -> navigateToFavoriteTeam()
                LoginResult.SignIn -> navigateToMain()
                is LoginResult.Failure -> binding.root.showSnackbar(R.string.login_failed_message)
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
}
