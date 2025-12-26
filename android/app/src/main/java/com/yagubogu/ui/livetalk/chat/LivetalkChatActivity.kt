package com.yagubogu.ui.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yagubogu.ui.theme.YaguBoguTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LivetalkChatActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelFactory: LivetalkChatViewModel.Factory

    private val viewModel: LivetalkChatViewModel by viewModels {
        val gameId = intent.getLongExtra(KEY_GAME_ID, 1L)
        val isVerified = intent.getBooleanExtra(KEY_IS_VERIFIED, false)
        LivetalkChatViewModel.provideFactory(viewModelFactory, gameId, isVerified)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaguBoguTheme {
                LivetalkChatScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                )
            }
        }
    }

    companion object {
        private const val KEY_GAME_ID = "gameId"
        private const val KEY_IS_VERIFIED = "isVerified"
        private const val KEY_TALK_DELETE_DIALOG = "talkDeleteDialog"
        private const val KEY_TALK_REPORT_DIALOG = "talkReportDialog"
        private const val MAX_ANIMATION_COUNT = 50

        fun newIntent(
            context: Context,
            gameId: Long,
            isVerified: Boolean,
        ): Intent =
            Intent(
                context,
                LivetalkChatActivity::class.java,
            ).apply {
                putExtra(KEY_GAME_ID, gameId)
                putExtra(KEY_IS_VERIFIED, isVerified)
            }
    }
}
