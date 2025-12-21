package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.presentation.livetalk.chat.LivetalkChatBubbleItem
import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import java.time.LocalDateTime

@Composable
fun LivetalkChatBubbleList(
    chatItems: List<LivetalkChatBubbleItem>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(
            items = chatItems,
            key = { it.livetalkChatItem.chatId },
        ) { item ->
            when (item) {
                is LivetalkChatBubbleItem.MyBubbleItem -> {
                    LivetalkMyChatBubble(
                        livetalkChatItem = item.livetalkChatItem,
                        onDeleteClick = {},
                    )
                }

                is LivetalkChatBubbleItem.OtherBubbleItem -> {
                    LivetalkOtherChatBubble(
                        livetalkChatItem = item.livetalkChatItem,
                        onReportClick = {},
                        onProfileClick = {},
                    )
                }
            }
        }
    }
}

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

@Preview
@Composable
private fun LivetalkChatBubbleListPreview() {
    LivetalkChatBubbleList(fixtureItems)
}
