package com.yagubogu.ui.badge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.yagubogu.YaguBoguApplication
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.theme.YaguBoguTheme

class BadgeActivity : ComponentActivity() {
    val viewModel: BadgeViewModel by viewModels { BadgeViewModelFactory((application as YaguBoguApplication).memberRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                BadgeScreen(
                    badgeUiState = viewModel.badgeUiState.value,
                    onBackClick = { finish() },
                    onRegisterClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, BadgeActivity::class.java)
    }
}
