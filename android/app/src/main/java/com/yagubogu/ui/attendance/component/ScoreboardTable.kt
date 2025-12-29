package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dpToSp

@Composable
fun ScoreboardTable(
    awayTeamName: String,
    homeTeamName: String,
    awayInningScores: List<String>,
    homeInningScores: List<String>,
    awayScore: Int,
    homeScore: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // 헤더
        ScoreboardRow(
            firstText = stringResource(R.string.attendance_history_team_name),
            values = (1..11).map { it.toString() } + "R",
            isHeader = true,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // Away 팀
        ScoreboardRow(
            firstText = awayTeamName,
            values = awayInningScores + awayScore.toString(),
            isHeader = false,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // Home 팀
        ScoreboardRow(
            firstText = homeTeamName,
            values = homeInningScores + homeScore.toString(),
            isHeader = false,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
    }
}

@Composable
private fun ScoreboardRow(
    firstText: String,
    values: List<String>,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(if (isHeader) Primary050 else White),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScoreboardText(
            text = firstText,
            isHeader = isHeader,
            modifier = Modifier.weight(1.6f),
        )
        VerticalDivider(color = Gray300, thickness = 0.4.dp)

        values.forEach { value: String ->
            ScoreboardText(
                text = value,
                isHeader = isHeader,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ScoreboardText(
    text: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style =
            if (isHeader) {
                PretendardRegular.copy(fontSize = 10.dpToSp, textAlign = TextAlign.Center)
            } else {
                PretendardMedium.copy(fontSize = 12.dpToSp, textAlign = TextAlign.Center)
            },
        modifier = modifier.padding(4.dp),
    )
}

@Preview
@Composable
private fun ScoreboardTablePreview() {
    ScoreboardTable(
        awayTeamName = "KIA",
        homeTeamName = "두산",
        awayInningScores = listOf("1", "0", "0", "2", "0", "0", "1", "0", "0", "-", "-"),
        homeInningScores = listOf("0", "1", "1", "0", "1", "0", "0", "2", "0", "-", "-"),
        awayScore = 10,
        homeScore = 4,
    )
}
