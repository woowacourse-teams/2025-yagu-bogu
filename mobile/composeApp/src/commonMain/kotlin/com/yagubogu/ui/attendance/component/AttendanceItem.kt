package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun AttendanceItem(
    item: AttendanceHistoryItem,
    onItemClick: (AttendanceHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = RoundedCornerShape(12.dp),
                    shadow =
                        Shadow(
                            radius = 1.dp,
                            offset = DpOffset(x = 0.dp, 1.dp),
                            alpha = 0.25f,
                        ),
                ).background(color = White, shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = Gray100, shape = RoundedCornerShape(12.dp))
                .noRippleClickable {
                    onItemClick(item)
                    AnalyticsLogger.logEvent("attendance_history_item_click")
                }.padding(20.dp),
    ) {
        GameResultSummary(item = item)

        ScoreboardTable(
            awayTeamName = item.awayTeam.name,
            homeTeamName = item.homeTeam.name,
            awayInningScores = item.awayTeamScoreBoard.inningScores,
            homeInningScores = item.homeTeamScoreBoard.inningScores,
            awayScore = item.awayTeamScoreBoard.runs,
            homeScore = item.homeTeamScoreBoard.runs,
        )
    }
}

@Preview(name = "완료된 경기")
@Composable
private fun AttendanceItemPlayedPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
        onItemClick = {},
    )
}

@Preview(name = "취소된 경기")
@Composable
private fun AttendanceItemCanceledPreview() {
    AttendanceItem(
        item = ATTENDANCE_HISTORY_ITEM_CANCELED,
        onItemClick = {},
    )
}
