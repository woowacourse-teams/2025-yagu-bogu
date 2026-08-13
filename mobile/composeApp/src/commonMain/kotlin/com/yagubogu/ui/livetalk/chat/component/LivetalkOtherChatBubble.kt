package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.crop
import com.yagubogu.ui.util.formatToAmPm
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_fan
import yagubogu.composeapp.generated.resources.all_null_nick_name
import yagubogu.composeapp.generated.resources.ic_flag
import yagubogu.composeapp.generated.resources.ic_heart
import yagubogu.composeapp.generated.resources.ic_heart_outline
import yagubogu.composeapp.generated.resources.ic_kebab
import yagubogu.composeapp.generated.resources.livetalk_kebab_icon_description
import yagubogu.composeapp.generated.resources.livetalk_like_icon_description
import yagubogu.composeapp.generated.resources.livetalk_reported_chat_message
import yagubogu.composeapp.generated.resources.livetalk_user_report_btn
import yagubogu.composeapp.generated.resources.livetalk_user_report_icon_description
import yagubogu.composeapp.generated.resources.time_am
import yagubogu.composeapp.generated.resources.time_pm

@Composable
fun LivetalkOtherChatBubble(
    livetalkChatItem: LivetalkChatItem,
    onReportClick: () -> Unit,
    onLikeClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(end = 30.dp)
                .background(
                    color = White,
                    shape = RoundedCornerShape(16.dp),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(bottom = 4.dp, end = 40.dp)
                        .noRippleClickable(onClick = onProfileClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileImage(
                    imageUrl = livetalkChatItem.profileImageUrl ?: "",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text =
                        livetalkChatItem.nickname
                            ?: stringResource(Res.string.all_null_nick_name),
                    style = PretendardBold16,
                    color = Black,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.all_fan, livetalkChatItem.teamName ?: ""),
                    style = PretendardMedium12,
                    color = Gray500,
                )
            }

            Text(
                text =
                    when {
                        livetalkChatItem.reported -> stringResource(Res.string.livetalk_reported_chat_message)
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
                        .defaultMinSize(minHeight = 38.dp)
                        .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        livetalkChatItem.timestamp.formatToAmPm(
                            amText = stringResource(Res.string.time_am),
                            pmText = stringResource(Res.string.time_pm),
                        ),
                    style = PretendardRegular12,
                    color = Gray500,
                )

                if (!livetalkChatItem.reported) {
                    LivetalkLikeButton(
                        isLiked = livetalkChatItem.isLiked,
                        likeCount = livetalkChatItem.likeCount,
                        onClick = onLikeClick,
                    )
                }
            }
        }

        if (!livetalkChatItem.reported) {
            LivetalkChatKebabMenu(
                onReportClick = onReportClick,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LivetalkChatKebabMenu(
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_kebab),
                contentDescription = stringResource(Res.string.livetalk_kebab_icon_description),
                tint = Gray500,
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            offset = DpOffset(0.dp, 4.dp),
            containerColor = White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.4.dp, Gray300),
            modifier =
                Modifier
                    .crop(vertical = 8.dp)
                    .padding(vertical = 4.dp),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.livetalk_user_report_btn),
                        style = PretendardRegular12.copy(color = Gray500),
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_flag),
                        contentDescription = stringResource(Res.string.livetalk_user_report_icon_description),
                        tint = Gray500,
                        modifier = Modifier.size(16.dp),
                    )
                },
                onClick = {
                    menuExpanded = false
                    onReportClick()
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.crop(horizontal = 0.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LivetalkLikeButton(
    isLiked: Boolean,
    likeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tintColor = if (isLiked) Red else Gray400
    val heartIcon = if (isLiked) Res.drawable.ic_heart else Res.drawable.ic_heart_outline
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(heartIcon),
            contentDescription = stringResource(Res.string.livetalk_like_icon_description),
            tint = tintColor,
            modifier = Modifier.size(16.dp),
        )

        if (likeCount > 0) {
            Text(
                text = "$likeCount",
                style = PretendardMedium,
                fontSize = 14.dpToSp,
                color = tintColor,
                modifier = Modifier.padding(start = 2.dp),
            )
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
        {},
    )
}

@Preview
@Composable
private fun LivetalkOtherLikedChatBubbleNotLikePreview() {
    LivetalkOtherChatBubble(
        LivetalkChatItem(
            0L,
            0L,
            false,
            "좋아요 하지 않은 채팅",
            null,
            "케인",
            "한화",
            LocalDateTime.now(),
            false,
            isLiked = false,
            likeCount = 12,
        ),
        {},
        {},
        {},
    )
}

@Preview
@Composable
private fun LivetalkOtherLikedChatBubbleLikePreview() {
    LivetalkOtherChatBubble(
        LivetalkChatItem(
            0L,
            0L,
            false,
            "좋아요한 채팅",
            null,
            "케인",
            "한화",
            LocalDateTime.now(),
            false,
            isLiked = true,
            likeCount = 13,
        ),
        {},
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
        {},
    )
}
