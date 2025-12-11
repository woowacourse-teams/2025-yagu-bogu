package com.yagubogu.ui.common.component.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.yagubogu.R
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.common.model.MEMBER_PROFILE_FIXTURE
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.util.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(
    onDismissRequest: () -> Unit,
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier =
            modifier
                .fillMaxWidth(0.9f)
                .background(Gray050, RoundedCornerShape(12.dp))
                .padding(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CancelButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End),
                )
                ProfileHeader(memberProfile = memberProfile)
                Spacer(modifier = Modifier.height(20.dp))
                ProfileContent(memberProfile = memberProfile)
            }
        },
    )
}

@Composable
private fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = Icons.Rounded.Close,
        contentDescription = stringResource(R.string.all_back_button_content_description),
        tint = Gray500,
        modifier = modifier.noRippleClickable(onClick = onClick),
    )
}

@Composable
fun ProfileHeader(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    val profileImageSize = 100.dp
    val profileImageOverlap = (0.6 * profileImageSize.value).dp // 카드 위로 겹치는 영역

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = profileImageOverlap)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(
                        top = profileImageOverlap - 20.dp,
                        bottom = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.all_fan, memberProfile.favoriteTeam),
                        style = PretendardMedium12,
                        color = Primary600,
                    )
                    Text(
                        text = memberProfile.nickname,
                        style = PretendardSemiBold20,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RankingText(memberProfile)
                }
                memberProfile.representativeBadgeImageUrl?.let { badgeImageUrl: String ->
                    AsyncImage(
                        model = badgeImageUrl,
                        contentDescription = stringResource(R.string.profile_representative_badge_content_description),
                        modifier = Modifier.size(50.dp),
                    )
                }
            }
        }
        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .background(Gray050, CircleShape)
                    .padding(10.dp),
        ) {
            ProfileImage(
                imageUrl = memberProfile.profileImageUrl,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun RankingText(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Row {
        Text(
            text = stringResource(R.string.profile_ranking_all),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
        Text(
            text = memberProfile.victoryFairyRanking?.toString() ?: "-",
            style = PretendardSemiBold,
            fontSize = 10.sp,
            color = Primary700,
        )
        Text(
            text = stringResource(R.string.profile_ranking_rank),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
        Text(
            text = "|",
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
            modifier = modifier.padding(horizontal = 6.dp),
        )
        Text(
            text = stringResource(R.string.all_fan, memberProfile.favoriteTeam),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
        Text(
            text = stringResource(R.string.profile_ranking_within_team),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
        Text(
            text = memberProfile.victoryFairyRankingWithinTeam?.toString() ?: "-",
            style = PretendardSemiBold,
            fontSize = 10.sp,
            color = Primary700,
        )
        Text(
            text = stringResource(R.string.profile_ranking_rank),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
    }
}

@Composable
fun ProfileContent(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
    ) {
        VictoryFairyStatsRow(memberProfile = memberProfile)
        Spacer(modifier = Modifier.height(24.dp))
        CheckInStatsRow(memberProfile = memberProfile, modifier = modifier)
        Spacer(modifier = Modifier.height(30.dp))
        DatesRow(memberProfile = memberProfile, modifier = modifier)
    }
}

@Composable
private fun VictoryFairyStatsRow(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
    ) {
        StatItem(
            title = stringResource(R.string.profile_victory_fairy_ranking),
            value =
                if (memberProfile.victoryFairyRanking != null) {
                    stringResource(R.string.all_ranking, memberProfile.victoryFairyRanking)
                } else {
                    null
                },
            emoji = stringResource(R.string.profile_victory_fairy_ranking_emoji),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = stringResource(R.string.profile_victory_fairy_score),
            value =
                if (memberProfile.victoryFairyScore != null) {
                    stringResource(R.string.all_score_first_float, memberProfile.victoryFairyScore)
                } else {
                    null
                },
            emoji = stringResource(R.string.profile_victory_fairy_score_emoji),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CheckInStatsRow(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
    ) {
        StatItem(
            title = stringResource(R.string.profile_check_in_counts),
            value = memberProfile.checkInCounts?.toString(),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = stringResource(R.string.profile_winning_percentage),
            value =
                if (memberProfile.checkInWinRate != null) {
                    stringResource(R.string.all_win_rate, memberProfile.checkInWinRate)
                } else {
                    null
                },
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = stringResource(R.string.profile_win_draw_lose),
            value = memberProfile.winDrawLose,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DatesRow(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
    ) {
        StatItem(
            title = stringResource(R.string.profile_register_date),
            value = memberProfile.enterDate.format(DateFormatter.yyyyMMdd),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = stringResource(R.string.profile_latest_check_in_date),
            value =
                memberProfile.recentCheckInDate
                    ?.format(DateFormatter.yyyyMMdd),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String?,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(text = value ?: "-", style = PretendardSemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = PretendardRegular, color = Gray500, fontSize = 10.sp)
    }
}

@Preview
@Composable
private fun ProfileHeaderPreview(modifier: Modifier = Modifier) {
    ProfileHeader(
        memberProfile = MEMBER_PROFILE_FIXTURE,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview(modifier: Modifier = Modifier) {
    ProfileContent(
        memberProfile = MEMBER_PROFILE_FIXTURE,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun StatItemPreview(modifier: Modifier = Modifier) {
    StatItem(
        title = "승리 요정 랭킹",
        value = "1424",
        modifier = modifier,
        emoji = "\uD83C\uDF96\uFE0F",
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileDialogPreview() {
    ProfileDialog(
        onDismissRequest = {},
        memberProfile = MEMBER_PROFILE_FIXTURE,
    )
}
