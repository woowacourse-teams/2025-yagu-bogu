package com.yagubogu.ui.badge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.yagubogu.YaguBoguApplication
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.theme.YaguBoguTheme

class BadgeActivity : ComponentActivity() {
    private val viewModel: BadgeViewModel by viewModels { BadgeViewModelFactory((application as YaguBoguApplication).memberRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view = LocalView.current
            LaunchedEffect(Unit) {
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
            }
            YaguBoguTheme {
                BadgeScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onRegisterClick = { badgeId: Long -> viewModel.updateRepresentativeBadge(badgeId) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, BadgeActivity::class.java)
    }
}
