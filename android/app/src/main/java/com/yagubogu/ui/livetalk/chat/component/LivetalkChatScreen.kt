package com.yagubogu.ui.livetalk.chat.component

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yagubogu.R
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.livetalk.chat.LivetalkChatViewModel
import com.yagubogu.ui.common.component.DefaultDialog
import com.yagubogu.ui.common.component.profile.ProfileDialog
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.util.emoji
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreen(
    viewModel: LivetalkChatViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messageStateHolder = viewModel.messageStateHolder
    val likeCountStateHolder = viewModel.likeCountStateHolder

    var emojiButtonPos by remember { mutableStateOf(Offset.Zero) }
    val emojiQueue = remember { mutableStateListOf<Pair<Long, Offset>>() }
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val messageText by messageStateHolder.messageText.collectAsStateWithLifecycle()
    val showingLikeCount by likeCountStateHolder.myTeamLikeShowingCount.collectAsStateWithLifecycle()
    val livetalkChatBubbleItems by messageStateHolder.livetalkChatBubbleItems.collectAsStateWithLifecycle()
    val pendingDeleteChat by messageStateHolder.pendingDeleteChat.collectAsStateWithLifecycle()
    val pendingReportChat by messageStateHolder.pendingReportChat.collectAsStateWithLifecycle()
    val clickedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()

    fun generateEmojiAnimation() {
        // 클릭 시점의 버튼 위치를 캡처해서 큐에 넣음
        emojiQueue.add(System.nanoTime() to emojiButtonPos)
    }
    Scaffold(
        topBar = {
            LivetalkChatToolbar(
                teams = teams,
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            LivetalkChatInputBar(
                messageFormText = messageText,
                stadiumName = teams?.stadiumName,
                isVerified = viewModel.messageStateHolder.isVerified,
                onTextChange = { viewModel.messageStateHolder.updateMessageText(it) },
                onSendMessage = { viewModel.sendMessage() },
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
                LivetalkChatBubbleList(
                    modifier = Modifier.weight(1f),
                    chatItems = livetalkChatBubbleItems,
                    onDeleteClick = viewModel.messageStateHolder::requestDelete,
                    onReportClick = viewModel.messageStateHolder::requestReport,
                    onProfileClick = { viewModel.fetchMemberProfile(it.memberId) },
                    fetchBeforeTalks = { viewModel.fetchBeforeTalks() },
                )

                // 구분선
                HorizontalDivider(thickness = max(0.4.dp, Dp.Hairline), color = Gray300)

                // 응원 바
                val myTeam = teams?.myTeam
                when {
                    myTeam != null && teams?.myTeamType != null -> {
                        LivetalkChatCheeringBar(
                            team = myTeam,
                            cheeringCount = showingLikeCount,
                            onCheeringClick = {
                                generateEmojiAnimation()
                                viewModel.addLikeToBatch()
                            },
                            onPositioned = { pos: Offset -> emojiButtonPos = pos },
                        )
                    }

                    else -> {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // 삭제 다이얼로그 레이어
            pendingDeleteChat?.let { chat ->
                DefaultDialog(
                    dialogUiModel =
                        DefaultDialogUiModel(
                            title = stringResource(R.string.livetalk_trash_btn),
                            message = stringResource(R.string.livetalk_trash_dialog_message),
                            positiveText = stringResource(R.string.livetalk_trash_btn),
                            negativeText = stringResource(R.string.all_cancel),
                        ),
                    onConfirm = {
                        viewModel.deleteMessage(
                            messageStateHolder.pendingDeleteChat.value?.chatId
                                ?: return@DefaultDialog,
                        )
                    },
                    onCancel = { messageStateHolder.dismissDeleteDialog() },
                )
            }

            // 신고 다이얼로그 레이어
            pendingReportChat?.let { chat ->
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
                        viewModel.reportMessage(
                            messageStateHolder.pendingReportChat.value?.chatId
                                ?: return@DefaultDialog,
                        )
                    },
                    onCancel = { messageStateHolder.dismissReportDialog() },
                )
            }

            // 프로필 다이얼로그 레이어
            clickedProfile?.let { profile ->
                ProfileDialog(
                    onDismissRequest = { viewModel.dismissProfile() },
                    memberProfile = profile,
                    modifier = modifier,
                )
            }

            // 이모지 애니메이션 레이어
            Box(modifier = Modifier.fillMaxSize()) {
                emojiQueue.forEach { (key, startPos) ->
                    key(key) {
                        LaunchedEffect(Unit) {
                            Timber.d("이모지 애니메이션 시작 좌표 : $startPos")
                        }
                        FloatingEmojiItem(
                            emoji =
                                viewModel.livetalkTeams.value
                                    ?.myTeam
                                    ?.emoji ?: "",
                            startOffset = startPos,
                            onAnimationFinished = { emojiQueue.remove(key to startPos) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LivetalkChatMyTeamScreenPreview() {
    LivetalkChatScreen(
        viewModel = viewModel(),
        onBackClick = {},
    )
}

@Preview
@Composable
private fun LivetalkChatOtherTeamScreenPreview() {
    LivetalkChatScreen(
        viewModel = viewModel(),
        onBackClick = {},
    )
}

@Preview
@Composable
private fun LivetalkChatLoadingScreenPreview() {
    LivetalkChatScreen(
        viewModel = viewModel(),
        onBackClick = {},
    )
}
