package com.yagubogu.ui.ranking.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.home.component.CHECK_IN_RANKING_ITEM
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING_ITEM
import com.yagubogu.ui.ranking.model.RankingProfileItem
import com.yagubogu.ui.theme.Bronze
import com.yagubogu.ui.theme.Gold
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Silver
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.formatOneDecimal
import com.yagubogu.ui.util.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_fan
import yagubogu.composeapp.generated.resources.home_check_in_count_format
import yagubogu.composeapp.generated.resources.home_victory_fairy_my_nickname
import yagubogu.composeapp.generated.resources.home_victory_fairy_score_format
import yagubogu.composeapp.generated.resources.ic_medal_first
import yagubogu.composeapp.generated.resources.ic_medal_second
import yagubogu.composeapp.generated.resources.ic_medal_third

@Composable
fun RankingMemberProfile(
    item: RankingProfileItem,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isMyRanking: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp)
                .noRippleClickable {
                    onClick(item.memberId)
                    AnalyticsLogger.logEvent("member_profile")
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.rank.toString(),
            style = PretendardRegular.copy(fontSize = 12.dpToSp, color = Gray500),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))

        ProfileImage(
            imageUrl = item.profileImageUrl,
            modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        if (isMyRanking) {
                            stringResource(
                                Res.string.home_victory_fairy_my_nickname,
                                item.nickname,
                            )
                        } else {
                            item.nickname
                        },
                    style = PretendardSemiBold16,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )
                if (item.rank in 1..3) {
                    Spacer(modifier = Modifier.width(6.dp))
                    RankMedal(
                        rank = item.rank.toInt(),
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.all_fan, item.teamName),
                style = PretendardMedium12.copy(color = Gray400),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text =
                when (item) {
                    is RankingProfileItem.CheckInRanking ->
                        stringResource(
                            Res.string.home_check_in_count_format,
                            item.count,
                        )

                    is RankingProfileItem.VictoryFairyRanking ->
                        stringResource(
                            Res.string.home_victory_fairy_score_format,
                            item.score.formatOneDecimal(),
                        )
                },
            style = PretendardRegular16,
        )
    }
}

@Composable
private fun RankMedal(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    val (iconRes: DrawableResource, color: Color) =
        when (rank) {
            1 -> Res.drawable.ic_medal_first to Gold
            2 -> Res.drawable.ic_medal_second to Silver
            3 -> Res.drawable.ic_medal_third to Bronze
            else -> null
        } ?: return

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun VictoryFairyRankingMemberProfilePreview() {
    RankingMemberProfile(item = VICTORY_FAIRY_RANKING_ITEM, onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun CheckInRankingMemberProfilePreview() {
    RankingMemberProfile(item = CHECK_IN_RANKING_ITEM, onClick = {})
}
