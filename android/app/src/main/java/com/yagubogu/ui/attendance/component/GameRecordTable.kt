package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import com.yagubogu.ui.theme.Primary900
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dsp

@Composable
fun GameRecordTable(
    awayTeamName: String,
    homeTeamName: String,
    awayHits: Int,
    homeHits: Int,
    awayErrors: Int,
    homeErrors: Int,
    awayBalls: Int,
    homeBalls: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // 헤더
        GameRecordRow(
            title = stringResource(R.string.attendance_history_team_record),
            firstValue = awayTeamName,
            secondValue = homeTeamName,
            isHeader = true,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // 안타
        GameRecordRow(
            title = stringResource(R.string.attendance_history_hits),
            firstValue = awayHits.toString(),
            secondValue = homeHits.toString(),
            isHeader = false,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // 실책
        GameRecordRow(
            title = stringResource(R.string.attendance_history_errors),
            firstValue = awayErrors.toString(),
            secondValue = homeErrors.toString(),
            isHeader = false,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)

        // 사사구
        GameRecordRow(
            title = stringResource(R.string.attendance_history_balls),
            firstValue = awayBalls.toString(),
            secondValue = homeBalls.toString(),
            isHeader = false,
        )
        HorizontalDivider(color = Gray300, thickness = 0.4.dp)
    }
}

@Composable
private fun GameRecordRow(
    title: String,
    firstValue: String,
    secondValue: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(if (isHeader) Primary050 else White),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameRecordText(
            text = firstValue,
            isTitle = false,
            isHeader = isHeader,
            modifier = Modifier.weight(1f),
        )
        GameRecordText(
            text = title,
            isTitle = true,
            isHeader = isHeader,
            modifier = Modifier.weight(1f),
        )
        GameRecordText(
            text = secondValue,
            isTitle = false,
            isHeader = isHeader,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GameRecordText(
    text: String,
    isTitle: Boolean,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style =
            if (isTitle) {
                PretendardMedium.copy(
                    fontSize = 12.dsp,
                    color = Primary900,
                    textAlign = TextAlign.Center,
                )
            } else if (isHeader) {
                PretendardRegular.copy(fontSize = 12.dsp, textAlign = TextAlign.Center)
            } else {
                PretendardMedium.copy(fontSize = 12.dsp, textAlign = TextAlign.Center)
            },
        modifier = modifier.padding(vertical = 8.dp),
    )
}

@Preview
@Composable
private fun GameRecordTablePreview() {
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
