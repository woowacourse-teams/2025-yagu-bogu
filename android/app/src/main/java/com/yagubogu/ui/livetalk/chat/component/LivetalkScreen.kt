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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.livetalk.chat.LivetalkChatBubbleItem
import com.yagubogu.presentation.util.getEmoji
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkChatScreen(
    onBackClick: () -> Unit,
    stadiumName: String?,
    myTeam: Team?,
    chatItems: List<LivetalkChatBubbleItem>,
    matchText: String?,
    modifier: Modifier = Modifier,
) {
    var emojiButtonPos by remember { mutableStateOf(Offset.Zero) }
    val emojiQueue = remember { mutableStateListOf<Pair<Long, Offset>>() }

    fun generateEmojiAnimation() {
        // 클릭 시점의 버튼 위치를 캡처해서 큐에 넣음
        emojiQueue.add(System.nanoTime() to emojiButtonPos)
    }
    Scaffold(
        topBar = { LivetalkChatToolbar(onBackClick = onBackClick, stadiumName, matchText) },
        bottomBar = {
            LivetalkChatInputBar(
                messageFormText = "임시 텍스트인 것이다",
                stadiumName = stadiumName,
                isVerified = true,
                onTextChange = {},
                onSendMessage = {},
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
                    chatItems = chatItems,
                    modifier = Modifier.weight(1f),
                )

                // 구분선
                HorizontalDivider(thickness = max(0.4.dp, Dp.Hairline), color = Gray300)

                // 응원 바
                when {
                    myTeam != null -> {
                        LivetalkChatCheeringBar(
                            team = Team.WO,
                            cheeringCount = 12345L,
                            onCheeringClick = { generateEmojiAnimation() },
                            onPositioned = { pos: Offset ->
                                emojiButtonPos = pos
                            },
                        )
                    }

                    else -> {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                emojiQueue.forEach { (key, startPos) ->
                    key(key) {
                        LaunchedEffect(Unit) {
                            Timber.d("이모지 애니메이션 시작 좌표 : $startPos")
                        }
                        FloatingEmojiItem(
                            emoji = myTeam?.getEmoji() ?: "",
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
        onBackClick = {},
        myTeam = Team.WO,
        chatItems = fixtureItems,
        stadiumName = "고척 스카이돔",
        matchText = "두산 vs 키움",
    )
}

@Preview
@Composable
private fun LivetalkChatOtherTeamScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        myTeam = null,
        chatItems = fixtureItems,
        stadiumName = "고척 스카이돔",
        matchText = "두산 vs 키움",
    )
}

@Preview
@Composable
private fun LivetalkChatLoadingScreenPreview() {
    LivetalkChatScreen(
        onBackClick = {},
        myTeam = null,
        chatItems = emptyList(),
        stadiumName = null,
        matchText = null,
    )
}
