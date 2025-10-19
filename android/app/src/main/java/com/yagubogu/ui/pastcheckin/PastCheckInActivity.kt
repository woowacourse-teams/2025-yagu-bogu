package com.yagubogu.ui.pastcheckin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yagubogu.YaguBoguApplication
import com.yagubogu.ui.pastcheckin.component.PastCheckInScreen
import com.yagubogu.ui.theme.YaguBoguTheme

class PastCheckInActivity : ComponentActivity() {
    private val viewModel: PastCheckInViewModel by viewModels {
        PastCheckInViewModelFactory((application as YaguBoguApplication).gamesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                PastCheckInScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, PastCheckInActivity::class.java)
    }
}
