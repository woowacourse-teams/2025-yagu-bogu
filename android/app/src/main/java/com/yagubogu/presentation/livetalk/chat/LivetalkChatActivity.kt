package com.yagubogu.presentation.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivityLivetalkChatBinding

class LivetalkChatActivity : AppCompatActivity() {
    private val binding: ActivityLivetalkChatBinding by lazy {
        ActivityLivetalkChatBinding.inflate(layoutInflater)
    }

    private val viewModel: LivetalkChatViewModel by viewModels {
        val app = application as YaguBoguApplication
        val gameId = intent.getLongExtra(KEY_GAME_ID, 1L)
        LivetalkChatViewModelFactory(gameId, app.talksRepository)
    }

    private val livetalkChatAdapter = LivetalkChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        setupRecyclerView()
        setupListener()
        setupObservers()
    }

    private fun setupListener() {
        binding.ivArrowLeft.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val chatLayoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = false
                reverseLayout = true
            }

        binding.rvChatMessages.apply {
            layoutManager = chatLayoutManager
            adapter = livetalkChatAdapter
            setHasFixedSize(true)
            itemAnimator = null
            clipToPadding = false
        }
    }

    private fun setupObservers() {
        viewModel.livetalkChatItems.observe(this) { value: List<LivetalkChatItem> ->
            val livetalkChatItems: List<LivetalkChatBubbleItem> =
                value.map { livetalkChatItem: LivetalkChatItem ->
                    LivetalkChatBubbleItem.of(livetalkChatItem)
                }
            livetalkChatAdapter.submitList(livetalkChatItems)
        }
    }

    companion object {
        private const val KEY_GAME_ID = "gameId"

        fun newIntent(
            context: Context,
            gameId: Long,
        ): Intent =
            Intent(context, LivetalkChatActivity::class.java).apply {
                putExtra(KEY_GAME_ID, gameId)
            }
    }
}
