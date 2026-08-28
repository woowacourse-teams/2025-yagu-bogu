package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.common.component.DiamondShape
import com.yagubogu.ui.livetalk.model.Condition
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.livetalk.model.toResource
import com.yagubogu.ui.livetalk.model.toStringResource
import com.yagubogu.ui.theme.EsamanruBold
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.Green
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.Red
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.Yellow
import com.yagubogu.ui.theme.dpToSp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.mascot
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerLoading
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_users
import yagubogu.composeapp.generated.resources.livetalk_stadium_select_arrow_description
import yagubogu.composeapp.generated.resources.livetalk_user_icon_description
import yagubogu.composeapp.generated.resources.livetalk_weather_icon_description
import kotlin.repeat

@Composable
fun LivetalkStadiumItem(
    item: LivetalkStadiumItem,
    onClick: (LivetalkStadiumItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (item.isVerified) Primary500 else Gray100,
                    shape = RoundedCornerShape(12.dp),
                ).noRippleClickable { onClick(item) }
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.stadiumName,
                    style = PretendardBold.copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.weatherUiModel != null) {
                        val weatherStatusText =
                            stringResource(item.weatherUiModel.condition.toStringResource())
                        IconWithText(
                            icon = item.weatherUiModel.condition.toResource(),
                            iconDescription =
                                stringResource(
                                    Res.string.livetalk_weather_icon_description,
                                    weatherStatusText,
                                ),
                            text = item.weatherUiModel.temperatureText,
                        )
                    }

                    IconWithText(
                        icon = Res.drawable.ic_users,
                        iconDescription = stringResource(Res.string.livetalk_user_icon_description),
                        text = item.userCount.toString(),
                    )
                }
            }

            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right),
                contentDescription = stringResource(Res.string.livetalk_stadium_select_arrow_description),
                tint = Gray500,
                modifier = Modifier.size(20.dp),
            )
        }

        StadiumLiveScores(item = item)
    }
}

@Composable
fun ShimmerStadiumItem(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(142.dp)
                .shimmerLoading(12.dp),
    )
}

@Composable
private fun StadiumLiveScores(item: LivetalkStadiumItem) {
    // TODO: 데이터 연동
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamItem(
            team = item.awayTeam,
            playerName = "김투수",
            modifier = Modifier.weight(1.0f),
        )
        Text(
            text = "vs",
            style = PretendardMedium.copy(fontSize = 20.dpToSp, color = Gray500),
        )
        LiveScoreBoard(
            awayTeamScore = 88,
            homeTeamScore = 1,
            inning = 6,
            firstBaseOccupied = true,
            secondBaseOccupied = false,
            thirdBaseOccupied = true,
            ballCount = 2,
            strikeCount = 1,
            outCount = 1,
        )

        TeamItem(
            team = item.homeTeam,
            playerName = "김타자",
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
private fun IconWithText(
    icon: DrawableResource,
    iconDescription: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = iconDescription,
            tint = Gray500,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = PretendardMedium12.copy(color = Gray500),
        )
    }
}

@Composable
private fun TeamItem(
    team: Team,
    playerName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(team.mascot),
            contentDescription = null,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .size(52.dp)
                    .background(team.color.copy(alpha = 0.2f))
                    .padding(8.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = team.shortname,
            style = PretendardSemiBold12,
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                // TODO: "투" or "타"
                text = playerName.first().toString(),
                style = PretendardMedium.copy(fontSize = 10.sp, color = Gray500),
            )
            Text(
                text = playerName,
                style = PretendardMedium12,
            )
        }
    }
}

@Composable
private fun LiveScoreBoard(
    awayTeamScore: Int,
    homeTeamScore: Int,
    inning: Int,
    firstBaseOccupied: Boolean,
    secondBaseOccupied: Boolean,
    thirdBaseOccupied: Boolean,
    ballCount: Int,
    strikeCount: Int,
    outCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(top = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = awayTeamScore.toString(),
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RunnerBases(
                    firstBaseOccupied = firstBaseOccupied,
                    secondBaseOccupied = secondBaseOccupied,
                    thirdBaseOccupied = thirdBaseOccupied,
                )
                Text(
                    text = inning.toString(),
                    style = PretendardSemiBold.copy(fontSize = 10.dpToSp, color = Primary600),
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(color = Primary100, shape = RoundedCornerShape(12.dp)),
                )
            }

            Text(
                text = homeTeamScore.toString(),
                style = EsamanruBold.copy(fontSize = 28.dpToSp, color = Gray700),
            )
        }

        BallStrikeOutCount(
            ballCount = ballCount,
            strikeCount = strikeCount,
            outCount = outCount,
        )
    }
}

@Composable
private fun RunnerBases(
    firstBaseOccupied: Boolean,
    secondBaseOccupied: Boolean,
    thirdBaseOccupied: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Base(isOccupied = secondBaseOccupied)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Base(isOccupied = thirdBaseOccupied)
            Base(isOccupied = firstBaseOccupied)
        }
    }
}

@Composable
private fun Base(
    isOccupied: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(14.dp)
                .background(
                    color = if (isOccupied) Yellow else Gray300,
                    shape = DiamondShape,
                ),
    )
}

@Composable
private fun BallStrikeOutCount(
    ballCount: Int,
    strikeCount: Int,
    outCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        CountRow(
            label = "B",
            count = ballCount,
            maxCount = 3,
            color = Green,
        )
        CountRow(
            label = "S",
            count = strikeCount,
            maxCount = 2,
            color = Yellow,
        )
        CountRow(
            label = "O",
            count = outCount,
            maxCount = 2,
            color = Red,
        )
    }
}

@Composable
private fun CountRow(
    label: String,
    count: Int,
    maxCount: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = PretendardSemiBold12.copy(color = Gray500),
        )

        repeat(maxCount) { index: Int ->
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .background(
                            color = if (index < count) color else Gray300,
                            shape = CircleShape,
                        ),
            )
        }
    }
}

@Preview
@Composable
private fun LivetalkStadiumItemVerifiedPreview() {
    LivetalkStadiumItem(
        item = LIVETALK_STADIUM_ITEM_VERIFIED,
        onClick = {},
    )
}

@Preview
@Composable
private fun LivetalkStadiumItemUnVerifiedPreview() {
    LivetalkStadiumItem(
        item =
            LIVETALK_STADIUM_ITEM_UNVERIFIED.copy(
                weatherUiModel =
                    WeatherUiModel(
                        1,
                        Condition.Clear,
                        "12.3°C",
                    ),
            ),
        onClick = {},
    )
}

@Preview
@Composable
private fun LivetalkStadiumItemShimmerPreview() {
    ShimmerStadiumItem()
}

@Preview
@Composable
private fun RunnerBasesPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            Modifier
                .background(White)
                .padding(20.dp),
    ) {
        RunnerBases(
            firstBaseOccupied = false,
            secondBaseOccupied = false,
            thirdBaseOccupied = false,
        )
        RunnerBases(
            firstBaseOccupied = true,
            secondBaseOccupied = false,
            thirdBaseOccupied = false,
        )
        RunnerBases(
            firstBaseOccupied = false,
            secondBaseOccupied = true,
            thirdBaseOccupied = true,
        )
        RunnerBases(
            firstBaseOccupied = true,
            secondBaseOccupied = true,
            thirdBaseOccupied = true,
        )
    }
}
