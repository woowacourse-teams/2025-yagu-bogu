package com.yagubogu.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yagubogu.ui.theme.TeamKia
import com.yagubogu.ui.theme.TeamKiwoom
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dsp
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.rememberBalloonBuilder

@Composable
fun StadiumFanRate(modifier: Modifier = Modifier) {
    val balloonBuilder = rememberBalloonBuilder(R.string.home_stadium_stats_tooltip)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                .padding(8.dp)
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
                    text = stringResource(R.string.home_stadium_stats_refresh_time, 14, 30),
                    style = PretendardRegular.copy(fontSize = 14.sp, color = Gray400),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = null,
                    tint = Gray400,
                    modifier =
                        Modifier
                            .padding(4.dp)
                            .size(20.dp)
                            .noRippleClickable {
                            },
                )
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            List(2) { StadiumFanRateItem() }
        }

        ShowMoreButton(isExpanded = false)
    }
}

@Composable
private fun StadiumFanRateItem(modifier: Modifier = Modifier) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier =
            modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp)),
    ) {
        Box {
            Row {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier =
                        Modifier
                            .weight(0.5f)
                            .background(color = TeamKia)
                            .padding(vertical = 18.dp)
                            .padding(start = 20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "KIA",
                            style = EsamanruMedium.copy(fontSize = 22.dsp, color = White),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "10.0%",
                            style = PretendardMedium.copy(fontSize = 16.dsp, color = White),
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier =
                        Modifier
                            .weight(0.5f)
                            .background(color = TeamKiwoom)
                            .padding(vertical = 18.dp)
                            .padding(end = 20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "키움",
                            style = EsamanruMedium.copy(fontSize = 22.dsp, color = White),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "10.0%",
                            style = PretendardMedium.copy(fontSize = 16.dsp, color = White),
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                StadiumFanRateDivider()
                Surface(
                    shadowElevation = 8.dp,
                    shape = CircleShape,
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
                                ).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StadiumFanRateDivider(modifier: Modifier = Modifier) {
    val width: Dp = 32.dp
    val skewed = 0.7f

    Box {
        Spacer(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .width(width)
                    .fillMaxHeight(0.5f)
                    .background(
                        color = TeamKia,
                        shape =
                            ParallelogramShape(
                                skewed = skewed / 2,
                                size = 0.dp,
                            ),
                    ),
        )
        Spacer(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .width(width)
                    .fillMaxHeight(0.5f)
                    .background(
                        color = TeamKiwoom,
                        shape =
                            ParallelogramShape(
                                skewed = skewed / 2,
                                size = 0.dp,
                            ),
                    ),
        )
        Spacer(
            modifier =
                Modifier
                    .width(width)
                    .fillMaxSize()
                    .background(
                        color = White,
                        shape =
                            ParallelogramShape(
                                skewed = skewed,
                                size = 0.dp,
                            ),
                    ),
        )
    }
}

@Preview
@Composable
private fun StadiumFanRatePreview() {
    StadiumFanRate()
}

@Preview
@Composable
private fun StadiumFanRateItemPreview() {
    StadiumFanRateItem()
}
