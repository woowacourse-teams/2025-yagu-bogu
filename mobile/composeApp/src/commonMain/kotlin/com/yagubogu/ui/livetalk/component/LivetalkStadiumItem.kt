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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.livetalk.model.Condition
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.model.WeatherUiModel
import com.yagubogu.ui.livetalk.model.toResource
import com.yagubogu.ui.livetalk.model.toStringResource
import com.yagubogu.ui.theme.EsamanruMedium
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
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
                    1.dp,
                    if (item.isVerified) Primary500 else Gray100,
                    RoundedCornerShape(12.dp),
                ).noRippleClickable { onClick(item) }
                .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.stadiumName,
                style = PretendardBold20,
            )
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier.weight(1.0f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconWithText(
                    icon = Res.drawable.ic_users,
                    iconDescription = stringResource(Res.string.livetalk_user_icon_description),
                    text = item.userCount.toString(),
                )
                if (item.weatherUiModel != null) {
                    val weatherStatusText = stringResource(item.weatherUiModel.condition.toStringResource())

                    IconWithText(
                        icon = item.weatherUiModel.condition.toResource(),
                        iconDescription = stringResource(Res.string.livetalk_weather_icon_description, weatherStatusText),
                        text = item.weatherUiModel.temperatureText,
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
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamItem(
                name = item.awayTeam.shortname,
                mascot = item.awayTeam.mascot,
                teamColor = item.awayTeam.color,
                modifier = Modifier.weight(1.0f),
            )
            Text(
                text = "vs",
                style = PretendardMedium.copy(fontSize = 20.dpToSp, color = Gray500),
            )
            TeamItem(
                name = item.homeTeam.shortname,
                mascot = item.homeTeam.mascot,
                teamColor = item.homeTeam.color,
                modifier = Modifier.weight(1.0f),
            )
        }
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
private fun IconWithText(
    icon: DrawableResource,
    iconDescription: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
    name: String,
    mascot: DrawableResource,
    teamColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(mascot),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = EsamanruMedium.copy(fontSize = 14.sp, color = teamColor),
        )
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
        item = LIVETALK_STADIUM_ITEM_UNVERIFIED.copy(weatherUiModel = WeatherUiModel(1, Condition.Clear, "12.3°C")),
        onClick = {},
    )
}

@Preview
@Composable
private fun LivetalkStadiumItemShimmerPreview() {
    ShimmerStadiumItem()
}
