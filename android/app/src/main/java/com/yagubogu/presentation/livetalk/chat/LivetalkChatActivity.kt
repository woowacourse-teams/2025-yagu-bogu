package com.yagubogu.presentation.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        setupBinding()
        setupRecyclerView()
        setupListener()
        setupObservers()
    }

    private fun setupBinding() {
        binding.viewModel = viewModel
        binding.isVerified = intent.getBooleanExtra(KEY_IS_VERIFIED, false)
        binding.lifecycleOwner = this
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

        livetalkChatAdapter.registerAdapterDataObserver(
            object :
                RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    binding.rvChatMessages.scrollToPosition(0)
                }
            },
        )
    }

    private fun setupObservers() {
        viewModel.liveTalkChatBubbleItem.observe(this) { livetalkChatBubbleItems: List<LivetalkChatBubbleItem> ->
            livetalkChatAdapter.submitList(livetalkChatBubbleItems)
        }
    }

    companion object {
        private const val KEY_GAME_ID = "gameId"
        private const val KEY_IS_VERIFIED = "isVerified"

        fun newIntent(
            context: Context,
            gameId: Long,
            isVerified: Boolean,
        ): Intent =
            Intent(context, LivetalkChatActivity::class.java).apply {
                putExtra(KEY_GAME_ID, gameId)
                putExtra(KEY_IS_VERIFIED, isVerified)
            }
    }
}
