package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.presentation.util.formatTimestamp
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.Primary900
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkMyChatBubble(
    modifier: Modifier = Modifier,
    livetalkChatItem: LivetalkChatItem,
    onDeleteClick: () -> Unit,
    isPending: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 30.dp)
                .background(
                    color = Primary050,
                    shape = RoundedCornerShape(16.dp),
                ).padding(top = 12.dp, start = 12.dp),
    ) {
        Text(
            text = livetalkChatItem.message,
            style = PretendardRegular16,
            color = Primary900,
            modifier =
                Modifier
                    .padding(end = 12.dp)
                    .fillMaxWidth(),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = livetalkChatItem.timestamp.formatTimestamp(),
                style = PretendardRegular12,
                color = Primary700,
            )

            when (isPending) {
                true -> {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.livetalk_pending_message),
                            style = PretendardRegular12,
                            color = Primary700,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }

                false -> {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onDeleteClick)
                                .background(Primary050)
                                .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash),
                            contentDescription = stringResource(id = R.string.livetalk_trash_icon_description),
                            tint = Primary700,
                            modifier =
                                Modifier
                                    .size(14.dp),
                        )

                        Text(
                            text = stringResource(id = R.string.livetalk_trash_btn),
                            style = PretendardRegular12,
                            color = Primary700,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LivetalkMyChatPendingBubblePreview() {
    LivetalkMyChatBubble(
        livetalkChatItem =
            LivetalkChatItem(
                0L,
                0L,
                true,
                "전송중인 텍스트인 것이다",
                null,
                null,
                null,
                LocalDateTime.now(),
                false,
            ),
        onDeleteClick = {},
        isPending = true,
    )
}

@Preview
@Composable
private fun LivetalkMyChatBubblePreview() {
    LivetalkMyChatBubble(
        livetalkChatItem =
            LivetalkChatItem(
                0L,
                0L,
                true,
                "짧은 텍스트인 것이다",
                null,
                null,
                null,
                LocalDateTime.now(),
                false,
            ),
        onDeleteClick = {},
    )
}

@Preview
@Composable
private fun LivetalkMyLongChatBubblePreview() {
    LivetalkMyChatBubble(
        livetalkChatItem =
            LivetalkChatItem(
                0L,
                0L,
                true,
                "요리보고 조리보고 알수없는 두리 두리 빙하타고 내려와 야구보구 만났지만 1억년전 야구보구 너무나 그리워 보고픈 야구보구 모두함께 떠나자 아아 아아 외로운 두리는 귀여운 야구보구",
                null,
                null,
                null,
                LocalDateTime.now(),
                false,
            ),
        onDeleteClick = {},
    )
}
