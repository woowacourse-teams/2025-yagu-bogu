package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.TeamType
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.MMddhhmmFormatter
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.mascot
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource

@Composable
fun GameResultSummary(
    item: AttendanceHistoryItem,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        GameTeam(
            team = item.awayTeam.team,
            name = item.awayTeam.name,
            type = item.awayTeam.type,
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = item.awayTeam.score,
                    style = EsamanruBold.copy(fontSize = 36.dpToSp, color = item.awayTeamColor),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ":",
                    style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
                )
                Text(
                    text = item.homeTeam.score,
                    style = EsamanruBold.copy(fontSize = 36.dpToSp, color = item.homeTeamColor),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.dateTime.format(MMddhhmmFormatter),
                style = PretendardRegular12.copy(color = Gray500),
            )
            Text(
                text = item.stadiumName,
                style = PretendardRegular12.copy(color = Gray500),
            )
        }

        GameTeam(
            team = item.homeTeam.team,
            name = item.homeTeam.name,
            type = item.homeTeam.type,
        )
    }
}

@Composable
private fun GameTeam(
    team: Team,
    name: String,
    type: TeamType,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(team.mascot),
            contentDescription = null,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .size(76.dp)
                    .background(team.color.copy(alpha = 0.2f))
                    .padding(8.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = PretendardSemiBold16,
        )
        Text(
            text = type.name,
            style = PretendardMedium.copy(fontSize = 10.sp, color = Gray400),
        )
    }
}

@Preview(name = "완료된 경기", showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GameResultPlayedPreview() {
    GameResultSummary(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
    )
}

@Preview(name = "취소된 경기", showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GameResultCanceledPreview() {
    GameResultSummary(
        item = ATTENDANCE_HISTORY_ITEM_CANCELED,
    )
}
