package com.yagubogu.ui.dialog.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yagubogu.R
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.dialog.model.MEMBER_PROFILE_FIXTURE
import com.yagubogu.ui.dialog.model.MemberProfile
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import kotlinx.datetime.toJavaLocalDate

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
        VictoryFairyStatsRow(memberProfile = memberProfile, modifier = modifier)
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
            title = "승리 요정 랭킹",
            value =
                if (memberProfile.victoryFairyRanking != null) {
                    stringResource(R.string.all_ranking, memberProfile.victoryFairyRanking)
                } else {
                    null
                },
            emoji = "\uD83C\uDF96\uFE0F",
            modifier = Modifier.weight(1f, true),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = "승리 요정 점수",
            value =
                if (memberProfile.victoryFairyScore != null) {
                    stringResource(R.string.all_score_first_float, memberProfile.victoryFairyScore)
                } else {
                    null
                },
            emoji = "\uD83E\uDDDA",
            modifier = Modifier.weight(1f, true),
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
            title = "응원팀 직관 횟수",
            value = memberProfile.checkInCounts?.toString(),
            modifier = Modifier.weight(1f, true),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = "직관 승률",
            value =
                if (memberProfile.checkInWinRate != null) {
                    stringResource(R.string.all_win_rate, memberProfile.checkInWinRate)
                } else {
                    null
                },
            modifier =
                Modifier.weight(
                    1f,
                    true,
                ),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = "승 무 패",
            value = memberProfile.winDrawLose,
            modifier = Modifier.weight(1f, true),
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
            title = "가입일",
            value = memberProfile.enterDate.toJavaLocalDate().format(DateFormatter.yyyyMMdd),
            modifier = Modifier.weight(1f, true),
        )
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.fillMaxHeight(0.8f),
        )
        StatItem(
            title = "최근 직관일",
            value =
                memberProfile.recentCheckInDate
                    ?.toJavaLocalDate()
                    ?.format(DateFormatter.yyyyMMdd),
            modifier = Modifier.weight(1f, true),
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
private fun ProfileContentPreview(modifier: Modifier = Modifier) {
    ProfileContent(
        memberProfile = MEMBER_PROFILE_FIXTURE,
        modifier = modifier,
    )
}
