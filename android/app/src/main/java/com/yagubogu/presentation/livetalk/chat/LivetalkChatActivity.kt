package com.yagubogu.presentation.livetalk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.databinding.ActivityLivetalkChatBinding
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.favorite.FavoriteTeamConfirmFragment
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatEvent
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.livetalk.chat.model.LivetalkUiState
import com.yagubogu.presentation.util.showSnackbar
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.common.model.MemberProfile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LivetalkChatActivity : AppCompatActivity() {
    private val binding: ActivityLivetalkChatBinding by lazy {
        ActivityLivetalkChatBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: LivetalkChatViewModel.Factory

    private val viewModel: LivetalkChatViewModel by viewModels {
        val gameId = intent.getLongExtra(KEY_GAME_ID, 1L)
        val isVerified = intent.getBooleanExtra(KEY_IS_VERIFIED, false)
        LivetalkChatViewModel.provideFactory(viewModelFactory, gameId, isVerified)
    }

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

                is LivetalkChatEvent.ViewProfile -> {
                    viewModel.fetchMemberProfile(event.livetalkChatItem.memberId)
                    firebaseAnalytics.logEvent("member_profile", null)
                }
            }
        }
    }

    private val chatLinearLayoutManager by lazy {
        binding.rvChatMessages.layoutManager as LinearLayoutManager
    }

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        setupBinding()
        setupListener()
        setupRecyclerView()
        setupObservers()
        setupComposeView()
        showMemberProfileDialog()
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

        binding.constraintBtnSend.setOnClickListener {
            viewModel.sendMessage()
            if (binding.editMessage.text.isNotBlank()) {
                firebaseAnalytics.logEvent("livetalk_send_message", null)
            }
        }
        setupLikeButton()
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
        viewModel.livetalkUiState.observe(this, ::handleLivetalkResponseUiState)
        viewModel.messageStateHolder.liveTalkChatBubbleItems.observe(
            this,
            ::handleLiveTalkChatBubbleItem,
        )
        viewModel.messageStateHolder.livetalkReportEvent.observe(this, ::handleLivetalkReportEvent)
        viewModel.messageStateHolder.livetalkDeleteEvent.observe(this) {
            binding.root.showSnackbar(R.string.livetalk_delete_succeed, R.id.divider)
        }
        observePollingLikeAnimation()
    }

    private fun observePollingLikeAnimation() {
        val likeButton = binding.tvLikeButton
        viewModel.likeCountStateHolder.myTeamLikeAnimationEvent.observe(this) { newLikesCount: Long ->
            if (newLikesCount <= 0) return@observe
            val animationCount: Int = minOf(MAX_ANIMATION_COUNT, newLikesCount.toInt())

            // 각 애니메이션이 담당할 기본 카운트 (몫)
            val baseIncrement: Long = newLikesCount / animationCount

            // 기본 카운트를 분배하고 남은 카운트 (나머지)
            val remainder = newLikesCount % animationCount

            lifecycleScope.launch {
                repeat(animationCount) { index: Int ->
                    launch {
                        // 남은 카운트(remainder)가 현재 인덱스보다 크면 1을 더해준다.
                        // 처음 'remainder' 개의 애니메이션이 1씩 더 담당
                        val increment: Long =
                            if (index < remainder) baseIncrement + 1 else baseIncrement

                        val randomDelay = (0L..10000L).random()
                        delay(randomDelay)

                        viewModel.likeCountStateHolder.increaseMyTeamShowingCount(increment)
                        showLikeEmojiAnimation(
                            viewModel.cachedLivetalkTeams.myTeamEmoji,
                            likeButton,
                        )
                    }
                }
            }
        }
        viewModel.likeCountStateHolder.otherTeamLikeAnimationEvent.observe(this) { newLikesCount: Long ->
            if (newLikesCount <= 0) return@observe
            val animationCount: Int = minOf(MAX_ANIMATION_COUNT, newLikesCount.toInt())

            lifecycleScope.launch {
                repeat(animationCount) { index: Int ->
                    launch {
                        val randomDelay = (0L..10000L).random()
                        delay(randomDelay)
                        showLikeEmojiAnimation(
                            viewModel.cachedLivetalkTeams.otherTeamEmoji,
                            likeButton,
                        )
                    }
                }
            }
        }
    }

    private fun setupLikeButton() {
        val likeButton = binding.tvLikeButton

        likeButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.likeCountStateHolder.increaseMyTeamShowingCount()
            }
            viewModel.addLikeToBatch()
            showLikeEmojiAnimation(likeButton.text.toString(), likeButton)
        }
    }

    private fun showLikeEmojiAnimation(
        emoji: String,
        likeButtonView: TextView,
    ) {
        val emojiView = binding.floatingEmojisView
        // 1. 버튼의 화면상 절대 좌표를 가져옵니다. (결과는 likeBtnPosition 배열에 저장됨)
        val likeBtnPosition = IntArray(2)
        likeButtonView.getLocationOnScreen(likeBtnPosition)

        // 2. 애니메이션 컨테이너(emojisView)의 화면상 절대 좌표를 가져옵니다.
        val containerPosition = IntArray(2)
        emojiView.getLocationOnScreen(containerPosition)

        // containerPosition을 빼서 상대 좌표를 정확히 계산합니다.
        val startX = (likeBtnPosition[0] - containerPosition[0]) + (likeButtonView.width / 2f)
        val startY = (likeBtnPosition[1] - containerPosition[1]) + (likeButtonView.height / 2f)

        emojiView.addLikeEmoji(emoji, startX, startY)
    }

    private fun handleLivetalkResponseUiState(uiState: LivetalkUiState) {
        if (uiState is LivetalkUiState.Error) {
            showToast(R.string.livetalk_loading_error)
            finish()
        }
    }

    private fun handleLiveTalkChatBubbleItem(livetalkChatBubbleItems: List<LivetalkChatBubbleItem>) {
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

    private fun handleLivetalkReportEvent(livetalkReportEvent: LivetalkReportEvent) {
        when (livetalkReportEvent) {
            LivetalkReportEvent.DuplicatedReport ->
                binding.root.showSnackbar(
                    R.string.livetalk_already_reported,
                    R.id.divider,
                )

            LivetalkReportEvent.Success ->
                binding.root.showSnackbar(
                    R.string.livetalk_report_succeed,
                    R.id.divider,
                )
        }
    }

    private fun showTalkDeleteDialog() {
        if (supportFragmentManager.findFragmentByTag(KEY_TALK_DELETE_DIALOG) == null) {
            val dialogUiModel =
                DefaultDialogUiModel(
                    title = getString(R.string.livetalk_trash_btn),
                    message = getString(R.string.livetalk_trash_dialog_message),
                    positiveText = getString(R.string.livetalk_trash_btn),
                )
            val dialog =
                DefaultDialogFragment.newInstance(KEY_TALK_DELETE_DIALOG, dialogUiModel)
            dialog.show(supportFragmentManager, KEY_TALK_DELETE_DIALOG)
        }
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

    private fun setupComposeView() {
        binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    private fun showMemberProfileDialog() {
        binding.composeView.setContent {
            var memberProfile by remember { mutableStateOf<MemberProfile?>(null) }

            LaunchedEffect(Unit) {
                viewModel.profileInfoClickEvent.collect { profile: MemberProfile ->
                    memberProfile = profile
                }
            }

            memberProfile?.let { profile ->
                ProfileDialog(
                    onDismissRequest = { memberProfile = null },
                    memberProfile = profile,
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
            Intent(context, LivetalkChatActivity::class.java).apply {
                putExtra(KEY_GAME_ID, gameId)
                putExtra(KEY_IS_VERIFIED, isVerified)
            }
    }
}
