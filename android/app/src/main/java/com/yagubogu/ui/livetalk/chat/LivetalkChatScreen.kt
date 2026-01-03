package com.yagubogu.ui.livetalk.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.livetalk.chat.component.EmptyLivetalkChat
import com.yagubogu.ui.livetalk.chat.component.FloatingEmojiItem
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatBubbleList
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatBubbleListShimmer
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatCheeringBar
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatInputBar
import com.yagubogu.ui.livetalk.chat.component.LivetalkChatToolbar
import com.yagubogu.ui.livetalk.chat.model.EmojiAnimationItem
import com.yagubogu.ui.livetalk.chat.model.LikeDeltaItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenActions
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatScreenStates
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatUiState
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.emoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreen(
    viewModel: LivetalkChatViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messageStateHolder = viewModel.messageStateHolder
    val likeCountStateHolder = viewModel.likeCountStateHolder
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val clickedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val chatUiState by viewModel.chatUiState.collectAsStateWithLifecycle()

    val messageText by messageStateHolder.messageText.collectAsStateWithLifecycle()
    val showingLikeCount by likeCountStateHolder.myTeamLikeShowingCount.collectAsStateWithLifecycle()
    val livetalkChatBubbleItems by messageStateHolder.livetalkChatBubbleItems.collectAsStateWithLifecycle()
    val pendingDeleteChat by messageStateHolder.pendingDeleteChat.collectAsStateWithLifecycle()
    val pendingReportChat by messageStateHolder.pendingReportChat.collectAsStateWithLifecycle()

    val emojiQueue = remember { mutableStateListOf<EmojiAnimationItem>() }
    var emojiButtonPos by remember { mutableStateOf(Offset.Zero) }

    fun generateEmojiAnimation(emoji: String) {
        // 클릭 시점의 버튼 위치를 캡처해서 큐에 넣음
        emojiQueue.add(EmojiAnimationItem(System.nanoTime(), emoji, emojiButtonPos))
    }

    val uiContentState =
        LivetalkChatScreenStates(
            messageText = messageText,
            showingLikeCount = showingLikeCount,
            livetalkChatBubbleItems = livetalkChatBubbleItems,
            pendingDeleteChat = pendingDeleteChat,
            pendingReportChat = pendingReportChat,
            emojiQueue = emojiQueue.toList(),
            teams = teams,
            clickedProfile = clickedProfile,
            chatUiState = chatUiState,
            isVerified = messageStateHolder.isVerified,
        )

    val actions =
        remember(viewModel, onBackClick) {
            LivetalkChatScreenActions(
                onMessageTextChange = messageStateHolder::updateMessageText,
                onRequestDelete = messageStateHolder::requestDelete,
                onRequestReport = messageStateHolder::requestReport,
                onDismissDeleteDialog = messageStateHolder::dismissDeleteDialog,
                onDismissReportDialog = messageStateHolder::dismissReportDialog,
                onBackClick = onBackClick,
                onEmojiButtonPositioned = { pos -> emojiButtonPos = pos },
                onCheeringClick = { emoji ->
                    generateEmojiAnimation(emoji)
                    viewModel.addLikeToBatch()
                },
                onAnimationFinished = { item -> emojiQueue.remove(item) },
                onSendMessage = viewModel::sendMessage,
                onFetchMemberProfile = viewModel::fetchMemberProfile,
                onFetchBeforeTalks = viewModel::fetchBeforeTalks,
                onDeleteMessage = viewModel::deleteMessage,
                onReportMessage = viewModel::reportMessage,
                onDismissProfile = viewModel::dismissProfile,
            )
        }

    LaunchedEffect(Unit) {
        viewModel.emojiAnimationSignal.collect { deltaItem: LikeDeltaItem ->
            // 1. 이모지 애니메이션 큐에 추가
            generateEmojiAnimation(deltaItem.emoji)

            // 2. 우리 팀의 변화량인 경우에만 UI 카운트 증가 (상대 팀은 카운트 텍스트가 없으므로 제외 가능)
            if (deltaItem.isMyTeam) {
                likeCountStateHolder.increaseMyTeamShowingCount(deltaItem.amount)
            }
        }
    }
    LaunchedEffect(Unit) {
        likeCountStateHolder.myTeamLikeChangeAmount.collect { count ->
            count?.let {
                // 1. 이모지 애니메이션 큐에 추가
                val myTeamEmoji = teams?.myTeamEmoji ?: return@collect
                scheduleEmojiAnimations(myTeamEmoji, count, this) {
                    generateEmojiAnimation(myTeamEmoji)
                }
                // 2. 우리 팀의 변화량인 경우에만 UI 카운트 증가 (상대 팀은 카운트 텍스트가 없으므로 제외 가능)
                likeCountStateHolder.increaseMyTeamShowingCount(count)
            }
        }
    }

    LaunchedEffect(Unit) {
        likeCountStateHolder.otherTeamLikeChangeAmount.collect { count ->
            count?.let {
                // 1. 이모지 애니메이션 큐에 추가
                val otherTeamEmoji = teams?.otherTeamEmoji ?: return@collect
                scheduleEmojiAnimations(otherTeamEmoji, count, this) {
                    generateEmojiAnimation(otherTeamEmoji)
                }
            }
        }
    }

    LivetalkChatScreenContent(
        state = uiContentState,
        actions = actions,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreenContent(
    state: LivetalkChatScreenStates,
    actions: LivetalkChatScreenActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            LivetalkChatToolbar(
                teams = state.teams,
                onBackClick = actions.onBackClick,
            )
        },
        bottomBar = {
            LivetalkChatInputBar(
                messageFormText = state.messageText,
                stadiumName = state.teams?.stadiumName,
                isVerified = state.isVerified,
                onTextChange = actions.onMessageTextChange,
                onSendMessage = actions.onSendMessage,
            )
        },
        containerColor = Gray050,
        modifier = modifier.background(Gray300),
    ) { innerPadding: PaddingValues ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
            ) {
                // 채팅 버블
                when (state.chatUiState) {
                    is LivetalkChatUiState.Success -> {
                        LivetalkChatBubbleList(
                            chatItems = state.livetalkChatBubbleItems,
                            modifier = Modifier.weight(1f),
                            onDeleteClick = actions.onRequestDelete,
                            onReportClick = actions.onRequestReport,
                            onProfileClick = { actions.onFetchMemberProfile(it.memberId) },
                            fetchBeforeTalks = { actions.onFetchBeforeTalks() },
                        )
                    }

                    is LivetalkChatUiState.Loading -> {
                        LivetalkChatBubbleListShimmer()
                    }

                    is LivetalkChatUiState.Empty -> {
                        EmptyLivetalkChat(isCheckIn = state.isVerified)
                    }
                }

                // 구분선
                HorizontalDivider(thickness = max(0.4.dp, Dp.Hairline), color = Gray300)

                // 응원 바
                val myTeam = state.teams?.myTeam
                when {
                    myTeam != null && state.teams.myTeamType != null -> {
                        LivetalkChatCheeringBar(
                            team = myTeam,
                            cheeringCount = state.showingLikeCount,
                            onCheeringClick = {
                                actions.onCheeringClick(myTeam.emoji)
                            },
                            onPositioned = actions.onEmojiButtonPositioned,
                        )
                    }

                    else -> {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // 삭제 다이얼로그 레이어
            state.pendingDeleteChat?.let { chat ->
                DefaultDialog(
                    dialogUiModel =
                        DefaultDialogUiModel(
                            title = stringResource(R.string.livetalk_trash_btn),
                            message = stringResource(R.string.livetalk_trash_dialog_message),
                            positiveText = stringResource(R.string.livetalk_trash_btn),
                            negativeText = stringResource(R.string.all_cancel),
                        ),
                    onConfirm = {
                        actions.onDeleteMessage(chat.chatId)
                    },
                    onCancel = actions.onDismissDeleteDialog,
                )
            }

            // 신고 다이얼로그 레이어
            state.pendingReportChat?.let { chat ->
                DefaultDialog(
                    dialogUiModel =
                        DefaultDialogUiModel(
                            title = stringResource(R.string.livetalk_user_report_btn),
                            message =
                                stringResource(
                                    R.string.livetalk_user_report_dialog_message,
                                    chat.nickname ?: stringResource(R.string.all_null_nick_name),
                                ),
                            positiveText = stringResource(R.string.livetalk_user_report_btn),
                            negativeText = stringResource(R.string.all_cancel),
                        ),
                    onConfirm = {
                        actions.onReportMessage(chat.chatId)
                    },
                    onCancel = actions.onDismissReportDialog,
                )
            }

            // 프로필 다이얼로그 레이어
            state.clickedProfile?.let { profile ->
                ProfileDialog(
                    onDismissRequest = { actions.onDismissProfile() },
                    memberProfile = profile,
                    modifier = modifier,
                )
            }

            // 이모지 애니메이션 레이어
            state.emojiQueue.forEach { item: EmojiAnimationItem ->
                key(item.id) {
                    LaunchedEffect(Unit) {
                        Timber.d("이모지 애니메이션 시작 좌표 : ${item.startOffset}")
                    }
                    FloatingEmojiItem(
                        emoji = item.emoji,
                        startOffset = item.startOffset,
                        onAnimationFinished = {
                            actions.onAnimationFinished(item)
                        },
                    )
                }
            }
        }
    }
}

private fun scheduleEmojiAnimations(
    emoji: String,
    count: Long,
    scope: CoroutineScope,
    triggerAnimation: () -> Unit,
) {
    if (count <= 0) return
    val animationCount: Int = minOf(MAX_ANIMATION_COUNT, count.toInt())

    // 각 애니메이션이 담당할 기본 카운트 (몫)
    val baseIncrement: Long = count / animationCount

    // 기본 카운트를 분배하고 남은 카운트 (나머지)
    val remainder = count % animationCount

    scope.launch {
        repeat(animationCount) { index: Int ->
            launch {
                // 남은 카운트(remainder)가 현재 인덱스보다 크면 1을 더해준다.
                // 처음 'remainder' 개의 애니메이션이 1씩 더 담당
                val increment: Long =
                    if (index < remainder) baseIncrement + 1 else baseIncrement

                val randomDelay = (0L..POLLING_INTERVAL_MILLS).random()
                delay(randomDelay)

                triggerAnimation()
                Timber.d("$emoji 이모지 애니메이션 및 $increment 만큼 카운트 증가")
            }
        }
    }
}

private const val MAX_ANIMATION_COUNT = 50
private const val POLLING_INTERVAL_MILLS = 10_000L

@Preview(showBackground = true, name = "KIA vs 한화")
@Composable
fun LivetalkChatPreviewSuccess() {
    // 1. 개별 채팅 데이터 생성 (기아와 한화 팬의 대화)
    val chat1 =
        LivetalkChatItem(
            chatId = 1L,
            memberId = 101L,
            isMine = false,
            message = "아사람 논란있는 사람아닌가요?",
            profileImageUrl = null,
            nickname = "무빙맨",
            teamName = "한화",
            timestamp = LocalDateTime.now().minusMinutes(5),
            reported = false,
        )

    val chat2 =
        LivetalkChatItem(
            chatId = 2L,
            memberId = 102L,
            isMine = true,
            message = "타이거즈 가즈아!",
            profileImageUrl = null,
            nickname = "포르",
            teamName = "기아",
            timestamp = LocalDateTime.now().minusMinutes(2),
            reported = false,
        )

    val mockChatBubbleItems =
        listOf(
            LivetalkChatBubbleItem.OtherBubbleItem(chat1),
            LivetalkChatBubbleItem.MyBubbleItem(chat2),
        )

    // 2. LivetalkTeams 구성
    // 홈팀: HT(KIA), 원정팀: HH, 내 팀: HT(KIA) (홈 팬 입장)
    val mockTeams =
        LivetalkTeams(
            stadiumName = "챔피언스 필드",
            homeTeamCode = "HT", // 기아 타이거즈
            awayTeamCode = "HH", // 한화 이글스
            myTeamCode = "HT", // 내 팀
        )

    val mockState =
        LivetalkChatScreenStates(
            messageText = "오늘 경기 직관 중인데 분위기 최고예요!",
            showingLikeCount = 1250L,
            livetalkChatBubbleItems = mockChatBubbleItems,
            teams = mockTeams,
            chatUiState = LivetalkChatUiState.Success(chatItems = mockChatBubbleItems),
            isVerified = true,
        )

    val mockActions = LivetalkChatScreenActions()

    YaguBoguTheme {
        LivetalkChatScreenContent(
            state = mockState,
            actions = mockActions,
        )
    }
}

@Preview(showBackground = true, name = "로딩 중")
@Composable
fun LivetalkChatPreviewLoading() {
    val mockState =
        LivetalkChatScreenStates(
            chatUiState = LivetalkChatUiState.Loading,
        )

    YaguBoguTheme {
        LivetalkChatScreenContent(
            state = mockState,
            actions = LivetalkChatScreenActions(),
        )
    }
}

// 내 팀이 홈/원정 어디에도 속하지 않고 인증하지 않은 경우
@Preview(showBackground = true, name = "채팅 없음 (비인증/제3자)")
@Composable
fun LivetalkChatPreviewEmpty() {
    val neutralTeams =
        LivetalkTeams(
            stadiumName = "고척 스카이돔",
            homeTeamCode = "KT", // 홈: KT
            awayTeamCode = "NC", // 원정: NC
            myTeamCode = "SS", // 내 팀: 삼성
        )

    val mockState =
        LivetalkChatScreenStates(
            chatUiState = LivetalkChatUiState.Empty,
            teams = neutralTeams,
            isVerified = false, // 인증되지 않은 상태
            messageText = "", // 입력창 비움
        )

    YaguBoguTheme {
        LivetalkChatScreenContent(
            state = mockState,
            actions = LivetalkChatScreenActions(),
        )
    }
}
