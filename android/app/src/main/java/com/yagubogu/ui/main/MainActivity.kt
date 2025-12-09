package com.yagubogu.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.yagubogu.databinding.ActivityMainBinding
import com.yagubogu.presentation.setting.SettingActivity
import com.yagubogu.ui.badge.BadgeActivity
import com.yagubogu.ui.theme.YaguBoguTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view: View = LocalView.current
            LaunchedEffect(Unit) {
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
            }
            YaguBoguTheme {
                MainScreen(
                    onBadgeClick = ::navigateToBadge,
                    onSettingsClick = ::navigateToSettings,
                )
            }
        }
    }

    fun setLoadingScreen(isLoading: Boolean) {
        val visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.viewOverlay.visibility = visibility
        binding.cpiCheckInLoading.visibility = visibility
    }

    private fun navigateToBadge() {
        val intent = BadgeActivity.newIntent(this)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = SettingActivity.newIntent(this)
        startActivity(intent)
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }
}
