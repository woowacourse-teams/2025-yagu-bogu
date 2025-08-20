package com.yagubogu.presentation.livetalk.chat

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.ActivityLivetalkChatBinding
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.favorite.FavoriteTeamConfirmFragment
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent

class LivetalkChatActivity : AppCompatActivity() {
    private val binding: ActivityLivetalkChatBinding by lazy {
        ActivityLivetalkChatBinding.inflate(layoutInflater)
    }

    private val viewModel: LivetalkChatViewModel by viewModels {
        val app = application as YaguBoguApplication
        val gameId = intent.getLongExtra(KEY_GAME_ID, 1L)
        LivetalkChatViewModelFactory(gameId, app.talksRepository)
    }

    private var talkDeleteDialog: DefaultDialogFragment? = null

    private var pendingDeleteMessageId: Long? = null
    private var pendingReportMessageId: Long? = null

    private val livetalkChatAdapter by lazy {
        LivetalkChatAdapter { event: LivetalkChatEvent ->
            when (event) {
                is LivetalkChatEvent.Delete -> {
                    pendingDeleteMessageId = event.livetalkChatItem.chatId
                    showTalkDeleteDialog()
                }

                is LivetalkChatEvent.Report -> {
                    pendingReportMessageId = event.livetalkChatItem.chatId
                    showTalkReportDialog(
                        event.livetalkChatItem.nickname ?: getString(R.string.all_null_nick_name),
                    )
                }
            }
        }
    }

    private fun showTalkDeleteDialog() {
        if (talkDeleteDialog == null) {
            val dialogUiModel =
                DefaultDialogUiModel(
                    title = getString(R.string.livetalk_trash_btn),
                    message = getString(R.string.livetalk_trash_dialog_message),
                    positiveText = getString(R.string.livetalk_trash_btn),
                )
            talkDeleteDialog =
                DefaultDialogFragment.newInstance(KEY_TALK_DELETE_DIALOG, dialogUiModel)
        }

        talkDeleteDialog?.show(supportFragmentManager, KEY_TALK_DELETE_DIALOG)
    }

    private fun showTalkReportDialog(reportTalkNickName: String) {
        val dialogUiModel =
            DefaultDialogUiModel(
                title = getString(R.string.livetalk_user_report_btn),
                message =
                    getString(
                        R.string.livetalk_user_report_dialog_message,
                        reportTalkNickName,
                    ),
                positiveText = getString(R.string.livetalk_user_report_btn),
            )
        val talkReportDialog =
            DefaultDialogFragment.newInstance(KEY_TALK_REPORT_DIALOG, dialogUiModel)
        talkReportDialog.show(supportFragmentManager, KEY_TALK_REPORT_DIALOG)
    }

    private val chatLinearLayoutManager by lazy {
        binding.rvChatMessages.layoutManager as LinearLayoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        setupBinding()
        setupListener()
        setupRecyclerView()
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

        binding.rvChatMessages.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    // 화면 최상단에 스크롤이 도달했을 때 과거 메시지 로드
                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.fetchBeforeTalks()
                    }
                }
            },
        )

        supportFragmentManager.setFragmentResultListener(
            KEY_TALK_DELETE_DIALOG,
            this,
        ) { _, bundle ->
            val isConfirmed: Boolean = bundle.getBoolean(FavoriteTeamConfirmFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.deleteMessage(pendingDeleteMessageId ?: return@setFragmentResultListener)
            }
        }

        supportFragmentManager.setFragmentResultListener(
            KEY_TALK_REPORT_DIALOG,
            this,
        ) { _, bundle ->
            val isConfirmed: Boolean = bundle.getBoolean(FavoriteTeamConfirmFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.reportMessage(pendingReportMessageId ?: return@setFragmentResultListener)
            }
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
        viewModel.liveTalkChatBubbleItem.observe(this) { livetalkChatBubbleItems: List<LivetalkChatBubbleItem> ->

            val oldFirstItemId =
                livetalkChatAdapter.currentList
                    .firstOrNull()
                    ?.livetalkChatItem
                    ?.chatId

            val firstVisibleItemPosition = chatLinearLayoutManager.findFirstVisibleItemPosition()

            livetalkChatAdapter.submitList(livetalkChatBubbleItems) {
                val newFirstItemId = livetalkChatBubbleItems.firstOrNull()?.livetalkChatItem?.chatId
                val isNewMessageArrived = oldFirstItemId != null && oldFirstItemId != newFirstItemId

                if (isNewMessageArrived && firstVisibleItemPosition == 0) {
                    chatLinearLayoutManager.scrollToPosition(0)
                }
            }
        }
        viewModel.livetalkReportEvent.observe(this) { livetalkReportEvent: LivetalkReportEvent ->
            when (livetalkReportEvent) {
                LivetalkReportEvent.DuplicatedReport -> showSnackbar(R.string.livetalk_already_reported)
                LivetalkReportEvent.Success -> showSnackbar(R.string.livetalk_report_succeed)
            }
        }

        viewModel.livetalkDeleteEvent.observe(this) {
            showSnackbar(R.string.livetalk_delete_succeed)
        }
    }

    private fun showSnackbar(
        @StringRes message: Int,
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(Color.DKGRAY)
            setTextColor(context.getColor(R.color.white))
            setAnchorView(binding.divider)
            show()
        }
    }

    companion object {
        private const val KEY_GAME_ID = "gameId"
        private const val KEY_IS_VERIFIED = "isVerified"
        private const val KEY_TALK_DELETE_DIALOG = "talkDeleteDialog"
        private const val KEY_TALK_REPORT_DIALOG = "talkReportDialog"

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
