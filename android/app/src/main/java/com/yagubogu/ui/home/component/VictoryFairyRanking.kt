package com.yagubogu.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.yagubogu.R
import com.yagubogu.ui.theme.Gold
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.theme.dsp
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.rememberBalloonBuilder

@Composable
fun VictoryFairyRanking(modifier: Modifier = Modifier) {
    val balloonBuilder = rememberBalloonBuilder(R.string.home_victory_fairy_tooltip)

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
                    text = stringResource(R.string.home_victory_fairy_ranking),
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
                                    Firebase.analytics.logEvent(
                                        "tooltip_victory_fairy_ranking",
                                        null,
                                    )
                                },
                    )
                }
            }
            Text(
                text = stringResource(R.string.home_victory_fairy_score),
                style = PretendardRegular.copy(fontSize = 14.sp, color = Gray400),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VictoryFairyRankingItem(onClick = {})
            HorizontalDivider(color = Gray300, thickness = 0.4.dp)

            List(5) {
                VictoryFairyRankingItem(onClick = {})
            }
        }
    }
}

@Composable
private fun VictoryFairyRankingItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "1",
            style = PretendardRegular.copy(fontSize = 16.dsp, color = Gray500),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))

        ProfileImage(
            imageUrl = "",
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "닉네암",
                    style = PretendardSemiBold16,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_medal_first),
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.height(14.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "KIA 팬",
                style = PretendardMedium12.copy(color = Gray400),
            )
        }

        Text(
            text = "100.0점",
            style = PretendardRegular16,
        )
    }
}

@Composable
private fun ProfileImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model =
            if (LocalInspectionMode.current) {
                R.drawable.ic_user
            } else {
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            },
        contentDescription = null,
        modifier =
            modifier
                .border(width = 1.dp, color = Gray300, shape = CircleShape)
                .padding(1.dp),
    )
}

@Preview
@Composable
private fun VictoryFairyRankingPreview() {
    VictoryFairyRanking()
}

@Preview(showBackground = true)
@Composable
private fun VictoryFairyRankingItemPreview() {
    VictoryFairyRankingItem(onClick = {})
}
