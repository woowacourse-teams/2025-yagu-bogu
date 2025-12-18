package com.yagubogu.ui.attendance.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun AttendanceItem(
    item: AttendanceHistoryItem,
    isExpanded: Boolean,
    onItemClick: (AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .noRippleClickable {
                    onItemClick(item)
                    Firebase.analytics.logEvent("attendance_history_item_click", null)
                }.padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        AttendanceHistorySummary(item = item.summary)
        when (item) {
            is AttendanceHistoryItem.Played -> {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    AttendanceHistoryDetail(item = item)
                }
            }

            is AttendanceHistoryItem.Canceled -> Unit
        }
    }
}

@Composable
private fun AttendanceHistorySummary(
    item: AttendanceHistoryItem.Summary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = item.awayTeam.score,
            style = EsamanruBold.copy(fontSize = 56.dpToSp),
            color = item.awayTeamColor,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.awayTeam.name,
                    style = PretendardSemiBold20,
                )
                Text(
                    text = "vs",
                    style = PretendardSemiBold16,
                )
                Text(
                    text = item.homeTeam.name,
                    style = PretendardSemiBold20,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.attendanceDate.format(DateFormatter.yyyyMMdd),
                style = PretendardRegular12.copy(color = Gray500),
            )
            Text(
                text = item.stadiumName,
                style = PretendardRegular.copy(fontSize = 10.sp, color = Gray500),
            )
        }

        Text(
            text = item.homeTeam.score,
            style = EsamanruBold.copy(fontSize = 56.dpToSp),
            color = item.homeTeamColor,
        )
    }
}

@Composable
private fun AttendanceHistoryDetail(
    item: AttendanceHistoryItem.Played,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(item.awayTeamPitcherStringRes, item.awayTeamPitcher),
                style = PretendardMedium12,
            )
            Text(
                text = stringResource(item.homeTeamPitcherStringRes, item.homeTeamPitcher),
                style = PretendardMedium12,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        ScoreboardTable(
            awayTeamName = item.awayTeam.name,
            homeTeamName = item.homeTeam.name,
            awayInningScores = item.awayTeamScoreBoard.inningScores,
            homeInningScores = item.homeTeamScoreBoard.inningScores,
            awayScore = item.awayTeamScoreBoard.runs,
            homeScore = item.homeTeamScoreBoard.runs,
        )
        Spacer(modifier = Modifier.height(20.dp))
        GameRecordTable(
            awayTeamName = item.awayTeam.name,
            homeTeamName = item.homeTeam.name,
            awayHits = item.awayTeamScoreBoard.hits,
            homeHits = item.homeTeamScoreBoard.hits,
            awayErrors = item.awayTeamScoreBoard.errors,
            homeErrors = item.homeTeamScoreBoard.errors,
            awayBalls = item.awayTeamScoreBoard.basesOnBalls,
            homeBalls = item.homeTeamScoreBoard.basesOnBalls,
        )
    }
}

@Preview(name = "완료된 경기")
@Composable
private fun AttendanceItemPlayedPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
        isExpanded = true,
        onItemClick = {},
    )
}

@Preview(name = "취소된 경기")
@Composable
private fun AttendanceItemCanceledPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_CANCELED,
        isExpanded = false,
        onItemClick = {},
    )
}
