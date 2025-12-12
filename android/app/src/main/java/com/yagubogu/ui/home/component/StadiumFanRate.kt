package com.yagubogu.ui.home.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.yagubogu.R
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.ui.common.component.HeartbeatAnimation
import com.yagubogu.ui.common.component.ShowMoreButton
import com.yagubogu.ui.common.component.shape.ParallelogramShape
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dsp
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.rememberBalloonBuilder

@Composable
fun StadiumFanRate(
    uiModel: StadiumStatsUiModel,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val balloonBuilder = rememberBalloonBuilder(R.string.home_stadium_stats_tooltip)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .padding(top = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_stadium_stats_title),
                    style = PretendardBold20,
                )
                Balloon(builder = balloonBuilder) { balloonWindow: BalloonWindow ->
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        tint = Gray300,
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .noRippleClickable {
                                    balloonWindow.showAlignBottom(yOff = -30)
                                    Firebase.analytics.logEvent("tooltip_stadium_stats", null)
                                },
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.home_stadium_stats_refresh_time,
                            uiModel.refreshTime.hour,
                            uiModel.refreshTime.minute,
                        ),
                    style = PretendardRegular.copy(fontSize = 14.sp, color = Gray400),
                )
                RefreshIcon(
                    onRefresh = {
                        onRefresh()
                        Firebase.analytics.logEvent("fan_rate_refresh", null)
                    },
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .padding(top = 20.dp)
                    .noRippleClickable(onClick)
                    .animateContentSize(),
        ) {
            when (isExpanded) {
                true -> uiModel.stadiumFanRates.forEach { StadiumFanRateItem(it) }
                false -> StadiumFanRateItem(uiModel.stadiumFanRates.first())
            }
        }

        if (uiModel.stadiumFanRates.size > 1) {
            ShowMoreButton(
                isExpanded = isExpanded,
                onClick = onClick,
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun RefreshIcon(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var rotation: Float by remember { mutableFloatStateOf(0f) }
    val animatedRotation: Float by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 1_000),
    )

    Icon(
        painter = painterResource(R.drawable.ic_refresh),
        contentDescription = null,
        tint = Gray400,
        modifier =
            modifier
                .padding(horizontal = 4.dp)
                .size(20.dp)
                .graphicsLayer {
                    rotationZ = animatedRotation
                }.noRippleClickable {
                    rotation += 360f
                    onRefresh()
                },
    )
}

@Composable
private fun StadiumFanRateItem(
    item: StadiumFanRateItem,
    modifier: Modifier = Modifier,
) {
    val itemHeight: Dp = 85.dp
    val awayTeamChartRange: Float = remapToChartRange(item.awayTeamPercentage).toFloat()
    val homeTeamChartRange: Float = remapToChartRange(item.homeTeamPercentage).toFloat()

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier =
            modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .height(itemHeight)
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp)),
    ) {
        BoxWithConstraints {
            Row {
                Column(
                    modifier =
                        Modifier
                            .weight(awayTeamChartRange)
                            .height(itemHeight)
                            .background(color = item.awayTeamFanRate.team.color)
                            .padding(vertical = 18.dp)
                            .padding(start = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = item.awayTeamFanRate.teamName,
                        style = EsamanruMedium.copy(fontSize = 22.dsp, color = White),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.all_win_rate, item.awayTeamPercentage),
                        style = PretendardMedium.copy(fontSize = 16.dsp, color = White),
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .weight(homeTeamChartRange)
                            .height(itemHeight)
                            .background(color = item.homeTeamFanRate.team.color)
                            .padding(vertical = 18.dp)
                            .padding(end = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = item.homeTeamFanRate.teamName,
                        style = EsamanruMedium.copy(fontSize = 22.dsp, color = White),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.all_win_rate, item.homeTeamPercentage),
                        style = PretendardMedium.copy(fontSize = 16.dsp, color = White),
                    )
                }
            }

            val centerOffset: Dp = maxWidth * awayTeamChartRange
            val dividerWidth: Dp = 32.dp
            StadiumFanRateDivider(
                awayTeamColor = item.awayTeamFanRate.team.color,
                homeTeamColor = item.homeTeamFanRate.team.color,
                width = dividerWidth,
                modifier = Modifier.offset(x = centerOffset - dividerWidth / 2),
            )
        }
    }
}

@Composable
private fun StadiumFanRateDivider(
    awayTeamColor: Color,
    homeTeamColor: Color,
    width: Dp,
    modifier: Modifier = Modifier,
    skewed: Float = 0.7f,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Spacer(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .width(width)
                    .fillMaxHeight(0.5f)
                    .background(
                        color = awayTeamColor,
                        shape = ParallelogramShape(skewed = skewed / 2, size = 0.dp),
                    ),
        )
        Spacer(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .width(width)
                    .fillMaxHeight(0.5f)
                    .background(
                        color = homeTeamColor,
                        shape = ParallelogramShape(skewed = skewed / 2, size = 0.dp),
                    ),
        )
        Spacer(
            modifier =
                Modifier
                    .width(width)
                    .fillMaxHeight()
                    .background(
                        color = White,
                        shape = ParallelogramShape(skewed = skewed, size = 0.dp),
                    ),
        )
        HeartbeatAnimation {
            Surface(
                shadowElevation = 8.dp,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(
                    text = "VS",
                    style = PretendardBold.copy(fontSize = 16.dsp, color = Gray700),
                    modifier =
                        Modifier
                            .background(color = White, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = Gray100,
                                shape = CircleShape,
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private fun remapToChartRange(percentage: Double): Double {
    val chartEndPaddingSize = 28.0
    val scalingFactor: Double = (100.0 - chartEndPaddingSize * 2) / 100.0
    val scaledRange: Double = chartEndPaddingSize + percentage.toFloat() * scalingFactor
    return scaledRange / 100.0
}

@Preview("경기 여러 개")
@Composable
private fun StadiumFanRatePreview() {
    StadiumFanRate(
        uiModel = STADIUM_STATS_UI_MODEL,
        isExpanded = false,
        onClick = {},
        onRefresh = {},
    )
}

@Preview("경기 한 개")
@Composable
private fun StadiumFanRateOnePreview() {
    StadiumFanRate(
        uiModel = StadiumStatsUiModel(stadiumFanRates = listOf(STADIUM_FAN_RATE_ITEM)),
        isExpanded = false,
        onClick = {},
        onRefresh = {},
    )
}

@Preview
@Composable
private fun StadiumFanRateItemPreview() {
    StadiumFanRateItem(item = STADIUM_FAN_RATE_ITEM)
}
