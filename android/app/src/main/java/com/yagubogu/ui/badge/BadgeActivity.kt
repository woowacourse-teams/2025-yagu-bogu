package com.yagubogu.ui.badge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.yagubogu.ui.badge.component.BadgeScreen
import com.yagubogu.ui.badge.model.BADGE_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BADGE_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.theme.YaguBoguTheme

class BadgeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                BadgeScreen(
                    mainBadge = null,
                    badgeList =
                        listOf(
                            BADGE_ACQUIRED_FIXTURE,
                            BADGE_NOT_ACQUIRED_FIXTURE,
                            BADGE_ACQUIRED_FIXTURE,
                            BADGE_ACQUIRED_FIXTURE,
                        ),
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
