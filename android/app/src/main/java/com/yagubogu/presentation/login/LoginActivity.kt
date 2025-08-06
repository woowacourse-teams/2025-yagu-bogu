package com.yagubogu.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.yagubogu.databinding.ActivityLoginBinding
import com.yagubogu.presentation.MainActivity
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var isAppInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplash()
        super.onCreate(savedInstanceState)
        setupView()
        setupListeners()
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

    private fun setupListeners() {
        binding.constraintBtnGoogle.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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
}
