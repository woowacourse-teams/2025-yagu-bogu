package com.yagubogu.ui.badge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.theme.YaguBoguTheme

class BadgeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                BadgeScreen(
                    mainBadge = null,
                    badgeList = listOf(),
                    onBackClick = { finish() },
                    onRegisterClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
