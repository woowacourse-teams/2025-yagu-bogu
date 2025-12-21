package com.yagubogu.ui.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatScreen
import com.yagubogu.ui.livetalk.chat.component.fixtureItems
import com.yagubogu.ui.theme.YaguBoguTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivetalkChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                LivetalkChatScreen(
                    onBackClick = { finish() },
                    myTeam = Team.WO,
                    chatItems = fixtureItems,
                    stadiumName = "고척 스카이돔",
                    matchText = "두산 vs 키움",
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, LivetalkChatActivity::class.java)
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YaguBoguTheme {
        Greeting("Android")
    }
}
