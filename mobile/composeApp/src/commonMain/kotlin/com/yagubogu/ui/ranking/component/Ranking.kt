package com.yagubogu.ui.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.home.component.CHECK_IN_RANKING
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.ranking.model.RankingProfileItem
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.BalloonTooltip
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_show_more
import yagubogu.composeapp.generated.resources.home_check_in_ranking
import yagubogu.composeapp.generated.resources.home_check_in_ranking_tooltip
import yagubogu.composeapp.generated.resources.home_victory_fairy_ranking
import yagubogu.composeapp.generated.resources.home_victory_fairy_ranking_tooltip
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_info

@Composable
fun Ranking(
    ranking: RankingUiModel,
    onRankingShowMoreClick: (RankingType) -> Unit,
    onMemberProfileClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        stringResource(
                            when (ranking.type) {
                                RankingType.CHECK_IN -> Res.string.home_check_in_ranking
                                RankingType.VICTORY_FAIRY -> Res.string.home_victory_fairy_ranking
                            },
                        ),
                    style = PretendardBold20,
                )
                BalloonTooltip(
                    text =
                        stringResource(
                            when (ranking.type) {
                                RankingType.CHECK_IN -> Res.string.home_check_in_ranking_tooltip
                                RankingType.VICTORY_FAIRY -> Res.string.home_victory_fairy_ranking_tooltip
                            },
                        ),
                ) { showTooltip ->
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = null,
                        tint = Gray300,
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .noRippleClickable {
                                    showTooltip()
                                    when (ranking.type) {
                                        RankingType.CHECK_IN ->
                                            AnalyticsLogger.logEvent("tooltip_check_in_ranking")

                                        RankingType.VICTORY_FAIRY ->
                                            AnalyticsLogger.logEvent("tooltip_victory_fairy_ranking")
                                    }
                                },
                    )
                }
            }

            if (ranking.hasNext) {
                RankingShowMore(
                    type = ranking.type,
                    onRankingShowMoreClick = onRankingShowMoreClick,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RankingLabel(ranking = ranking)

            RankingMemberProfile(
                item = ranking.myRanking,
                onClick = onMemberProfileClick,
                isMyRanking = true,
            )
            HorizontalDivider(color = Gray300, thickness = 0.4.dp)

            ranking.topRankings.forEach { item: RankingProfileItem ->
                RankingMemberProfile(
                    item = item,
                    onClick = onMemberProfileClick,
                )
            }
        }
    }
}

@Composable
private fun RankingShowMore(
    type: RankingType,
    onRankingShowMoreClick: (RankingType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier.noRippleClickable {
                onRankingShowMoreClick(type)
            },
    ) {
        Text(
            text = stringResource(Res.string.all_show_more),
            style = PretendardMedium.copy(fontSize = 14.sp, color = Gray400),
        )
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(Res.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Gray400,
        )
    }
}

@Preview
@Composable
private fun CheckInRankingPreview() {
    Ranking(
        ranking = CHECK_IN_RANKING,
        onRankingShowMoreClick = {},
        onMemberProfileClick = {},
    )
}

@Preview
@Composable
private fun VictoryFairyRankingPreview() {
    Ranking(
        ranking = VICTORY_FAIRY_RANKING,
        onRankingShowMoreClick = {},
        onMemberProfileClick = {},
    )
}
