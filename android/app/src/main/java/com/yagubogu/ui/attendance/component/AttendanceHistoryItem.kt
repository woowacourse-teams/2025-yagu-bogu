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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.presentation.attendance.model.AttendanceHistoryUiModel
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.TeamDoosan
import com.yagubogu.ui.theme.TeamKia
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dsp
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun AttendanceHistoryItem(
    item: AttendanceHistoryUiModel,
    onSummaryItemClick: (AttendanceHistoryUiModel.Summary) -> Unit,
    onDetailItemClick: (AttendanceHistoryUiModel.Detail) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .noRippleClickable {
                    when (item) {
                        is AttendanceHistoryUiModel.Summary -> onSummaryItemClick(item)
                        is AttendanceHistoryUiModel.Detail -> onDetailItemClick(item)
                        is AttendanceHistoryUiModel.Canceled -> Unit
                    }
                }.padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        AttendanceHistorySummary()
        AnimatedVisibility(
            visible = item is AttendanceHistoryUiModel.Detail,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            AttendanceHistoryDetail()
        }
    }
}

@Composable
private fun AttendanceHistorySummary(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "9",
            style = EsamanruBold.copy(fontSize = 56.dsp),
            color = TeamKia,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "KIA",
                    style = PretendardSemiBold20,
                )
                Text(
                    text = "vs",
                    style = PretendardSemiBold16,
                )
                Text(
                    text = "두산",
                    style = PretendardSemiBold20,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "2025.08.01",
                style = PretendardRegular12.copy(color = Gray500),
            )
            Text(
                text = "잠실 야구장",
                style = PretendardRegular.copy(fontSize = 10.sp, color = Gray500),
            )
        }

        Text(
            text = "5",
            style = EsamanruBold.copy(fontSize = 56.dsp),
            color = TeamDoosan,
        )
    }
}

@Composable
private fun AttendanceHistoryDetail(modifier: Modifier = Modifier) {
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
                text = "승 - 이의리",
                style = PretendardMedium12,
            )
            Text(
                text = "패 - 김택연",
                style = PretendardMedium12,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        ScoreboardTable(
            awayTeam = "KIA",
            homeTeam = "두산",
            awayInningScores = listOf("1", "0", "0", "2", "0", "0", "1", "0", "0", "-", "-"),
            homeInningScores = listOf("0", "1", "1", "0", "1", "0", "0", "2", "0", "-", "-"),
            awayScore = "10",
            homeScore = "4",
        )
        Spacer(modifier = Modifier.height(20.dp))
        GameRecordTable(
            awayTeamName = "KIA",
            homeTeamName = "두산",
            awayHits = 13,
            homeHits = 9,
            awayErrors = 0,
            homeErrors = 2,
            awayBalls = 5,
            homeBalls = 3,
        )
    }
}

@Preview
@Composable
private fun AttendanceHistoryItemPreview() {
//    AttendanceHistoryItem()
}
