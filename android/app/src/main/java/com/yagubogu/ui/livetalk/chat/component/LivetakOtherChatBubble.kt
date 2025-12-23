package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yagubogu.R
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.presentation.util.formatTimestamp
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.White
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivetalkOtherChatBubble(
    livetalkChatItem: LivetalkChatItem,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(end = 30.dp)
                .background(
                    color = White,
                    shape = RoundedCornerShape(16.dp),
                ).padding(top = 12.dp, start = 12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(bottom = 4.dp)
                    .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = livetalkChatItem.profileImageUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, Gray300, CircleShape),
                placeholder = painterResource(R.drawable.ic_users),
                error = painterResource(R.drawable.ic_users),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = livetalkChatItem.nickname ?: "알 수 없음",
                style = PretendardBold16,
                color = Black,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.all_fan, livetalkChatItem.teamName ?: ""),
                style = PretendardMedium12,
                color = Gray500,
            )
        }

        Text(
            text =
                when {
                    livetalkChatItem.reported -> stringResource(id = R.string.livetalk_reported_chat_message)
                    else -> livetalkChatItem.message
                },
            style = PretendardRegular16,
            color = if (livetalkChatItem.reported) Gray400 else Gray900,
            modifier =
                Modifier
                    .padding(end = 12.dp)
                    .fillMaxWidth(),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 38.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 시간
            Text(
                text = livetalkChatItem.timestamp.formatTimestamp(),
                style = PretendardRegular12,
                color = Gray500,
            )

            if (!livetalkChatItem.reported) {
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onReportClick)
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flag),
                        contentDescription = stringResource(id = R.string.livetalk_user_report_icon_description),
                        tint = Gray500,
                        modifier =
                            Modifier
                                .size(14.dp),
                    )

                    Text(
                        text = stringResource(id = R.string.livetalk_user_report_btn),
                        style = PretendardRegular12,
                        color = Gray500,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LivetalkOtherChatBubblePreview() {
    LivetalkOtherChatBubble(
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
        ),
        {},
        {},
    )
}

@Preview
@Composable
private fun LivetalkOtherLongChatBubblePreview() {
    LivetalkOtherChatBubble(
        LivetalkChatItem(
            0L,
            0L,
            false,
            "요리보고 조리보고 알수없는 두리 두리 빙하타고 내려와 야구보구 만났지만 1억년전 야구보구 너무나 그리워 보고픈 야구보구 모두함께 떠나자 아아 아아 외로운 두리는 귀여운 야구보구",
            null,
            "케인",
            "한화",
            LocalDateTime.now(),
            false,
        ),
        {},
        {},
    )
}

@Preview
@Composable
private fun LivetalkReportedOtherLongChatBubblePreview() {
    LivetalkOtherChatBubble(
        LivetalkChatItem(
            0L,
            0L,
            false,
            "신고당한 채팅인 것이다",
            null,
            "케인",
            "한화",
            LocalDateTime.now(),
            true,
        ),
        {},
        {},
    )
}
