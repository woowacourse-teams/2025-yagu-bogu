package com.yagubogu.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.yagubogu.R
import com.yagubogu.ui.home.model.MemberStatsUiModel
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White

@Composable
fun MemberStats(
    uiModel: MemberStatsUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        MemberStatsItem(
            title = stringResource(R.string.home_my_team),
            value = uiModel.myTeam ?: "",
            modifier = Modifier.weight(1f),
        )
        MemberStatsItem(
            title = stringResource(R.string.home_attendance_count),
            value = uiModel.attendanceCount.toString(),
            modifier = Modifier.weight(1f),
        )
        MemberStatsItem(
            title = stringResource(R.string.home_winning_percentage),
            value = stringResource(R.string.all_rounded_win_rate, uiModel.winRate),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MemberStatsItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .border(width = 1.dp, color = Primary100, shape = RoundedCornerShape(12.dp))
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = PretendardBold20.copy(color = Primary700),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = PretendardRegular12.copy(color = Gray500),
        )
    }
}

@Preview
@Composable
private fun MemberStatsItemPreview() {
    MemberStatsItem(title = "우리 팀", value = "KIA")
}
