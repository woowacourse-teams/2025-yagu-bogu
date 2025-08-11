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
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        setupRecyclerView()
        setupListener()
        setupObservers()
    }

    private fun setupListener() {
        viewModel.startChatPolling()

        binding.ivArrowLeft.setOnClickListener {
            finish()
        }

        binding.rvChatMessages.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    // 스크롤을 위로 올렸고, 리스트의 최상단에 도달했으며, 로딩 중이 아닐 때
                    if (!recyclerView.canScrollVertically(-1)) {
                        if (viewModel.isLoading.value == false) {
                            viewModel.fetchBeforeTalks()
                        }
                    }
                    // 스크롤을 아래로 내려 최하단에 도달하면 폴링 시작
                    if (!recyclerView.canScrollVertically(1)) {
                        viewModel.startChatPolling()
                    } else {
                        // 최하단이 아니면 폴링 중지
                        viewModel.stopChatPolling()
                    }
                }
            },
        )
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
        viewModel.liveTalkChatBubbleItem.observe(this) { livetalkChatBubbleItems: List<LivetalkChatBubbleItem> ->
            val layoutManager = binding.rvChatMessages.layoutManager as LinearLayoutManager

            val oldFirstItemId =
                livetalkChatAdapter.currentList
                    .firstOrNull()
                    ?.livetalkChatItem
                    ?.chatId

            livetalkChatAdapter.submitList(livetalkChatBubbleItems) {
                val newFirstItemId = livetalkChatBubbleItems.firstOrNull()?.livetalkChatItem?.chatId
                val isNewMessageArrived = oldFirstItemId != null && oldFirstItemId != newFirstItemId

                if (isNewMessageArrived) {
                    layoutManager.scrollToPosition(0)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopChatPolling()
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
