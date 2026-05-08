package com.yagubogu.ui.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.PretendardRegular12
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.home_check_in_count
import yagubogu.composeapp.generated.resources.home_user_profile
import yagubogu.composeapp.generated.resources.home_victory_fairy_score

@Composable
fun RankingLabel(
    ranking: RankingUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Gray100)
                .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Text(
            text = stringResource(Res.string.home_user_profile),
            style = PretendardRegular12,
        )
        Text(
            text =
                stringResource(
                    when (ranking.type) {
                        RankingType.CHECK_IN -> Res.string.home_check_in_count
                        RankingType.VICTORY_FAIRY -> Res.string.home_victory_fairy_score
                    },
                ),
            style = PretendardRegular12,
        )
    }
}

@Preview
@Composable
private fun RankingLabelPreview() {
    RankingLabel(ranking = RankingUiModel(type = RankingType.CHECK_IN))
}
