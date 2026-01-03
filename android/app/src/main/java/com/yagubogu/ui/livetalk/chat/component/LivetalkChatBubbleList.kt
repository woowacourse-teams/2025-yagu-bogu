package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * 현장톡 채팅 메시지 목록을 표시하는 컴포저블
 *
 * 역순 레이아웃으로 최신 메시지(0번 인덱스)가 화면 하단에 표시되며,
 * 최근 채팅을 보는 중, 새 메시지 도착 시 자동 스크롤과 무한 스크롤 페이징을 지원합니다.
 *
 * @param chatItems 표시할 채팅 메시지 아이템 리스트
 * @param modifier 컴포저블에 적용할 Modifier
 * @param fetchBeforeTalks 이전 메시지를 불러오는 콜백 (무한 스크롤)
 * @param onDeleteClick 내 메시지 삭제 클릭 시 호출되는 콜백
 * @param onReportClick 다른 사용자 메시지 신고 클릭 시 호출되는 콜백
 * @param onProfileClick 다른 사용자 프로필 클릭 시 호출되는 콜백
 * @param listState LazyColumn의 스크롤 상태를 제어하는 LazyListState
 */
@Composable
fun LivetalkChatBubbleList(
    chatItems: List<LivetalkChatBubbleItem>,
    modifier: Modifier = Modifier,
    fetchBeforeTalks: () -> Unit = {},
    onDeleteClick: (LivetalkChatItem) -> Unit = {},
    onReportClick: (LivetalkChatItem) -> Unit = {},
    onProfileClick: (LivetalkChatItem) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    // 이전 리컴포지션 시점의 채팅 아이템 개수 (새 메시지 감지용)
    var previousItemsCount by remember { mutableIntStateOf(chatItems.size) }

    // 새 메시지가 추가되고 사용자가 최근 채팅(~5개)을 보고 있으면 자동으로 0번 메시지로 스크롤
    LaunchedEffect(chatItems.size) {
        if (chatItems.size > previousItemsCount) {
            val isNearLatest = listState.firstVisibleItemIndex <= 5

            if (isNearLatest) {
                listState.animateScrollToItem(0)
            }
        }
        previousItemsCount = chatItems.size
    }

    // 사용자가 과거 메시지 방향으로 스크롤하여 5개 이하의 과거 메시지가 남았을 때 fetchBeforeTalks 호출
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems: List<LazyListItemInfo> ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                val totalItems = listState.layoutInfo.totalItemsCount

                val shouldFetchBeforeTalks =
                    totalItems > 0 && lastVisibleItemIndex >= totalItems - 5
                shouldFetchBeforeTalks
            }.distinctUntilChanged()
            .filter { it }
            .collect {
                fetchBeforeTalks()
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom),
    ) {
        items(
            items = chatItems,
            key = { it.livetalkChatItem.chatId },
        ) { item ->
            when (item) {
                is LivetalkChatBubbleItem.MyBubbleItem -> {
                    LivetalkMyChatBubble(
                        livetalkChatItem = item.livetalkChatItem,
                        onDeleteClick = { onDeleteClick(item.livetalkChatItem) },
                    )
                }

                is LivetalkChatBubbleItem.OtherBubbleItem -> {
                    LivetalkOtherChatBubble(
                        livetalkChatItem = item.livetalkChatItem,
                        onReportClick = { onReportClick(item.livetalkChatItem) },
                        onProfileClick = { onProfileClick(item.livetalkChatItem) },
                    )
                }

                is LivetalkChatBubbleItem.MyPendingBubbleItem -> {
                    LivetalkMyChatBubble(
                        livetalkChatItem = item.livetalkChatItem,
                        onDeleteClick = { onDeleteClick(item.livetalkChatItem) },
                        isPending = true,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LivetalkChatBubbleListPreview() {
    val otherChat =
        LivetalkChatItem(
            0L,
            0L,
            false,
            "짧은 텍스트인 것이다",
            null,
            "케인",
            "한화",
            LocalDateTime.now(),
            false,
        )

    val otherLongChat =
        otherChat.copy(message = "한화의 김성근 감독님 사랑해 예 예 예 예예예 예 예예예 예 예 예 예예예 예~ 한화의 김성근 감독님 사랑해")

    val myChat = otherChat.copy(isMine = true)

    val reportedChat = otherChat.copy(reported = true)

    val fixtureItems =
        listOf(
            otherChat.copy(chatId = 1L),
            otherLongChat.copy(chatId = 2L),
            reportedChat.copy(chatId = 3L),
            myChat.copy(chatId = 4L),
            otherLongChat.copy(chatId = 5L),
            myChat.copy(chatId = 6L),
            otherLongChat.copy(chatId = 7L),
            otherLongChat.copy(chatId = 8L),
            myChat.copy(chatId = 9L),
            otherLongChat.copy(chatId = 10L),
        ).map { LivetalkChatBubbleItem.of(it) }
    LivetalkChatBubbleList(chatItems = fixtureItems)
}
