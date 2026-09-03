package com.yagubogu.ui.livetalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import com.yagubogu.ui.livetalk.model.toResource
import com.yagubogu.ui.livetalk.model.toStringResource
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
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
    item: LivetalkStadiumUiModel,
    onClick: (LivetalkStadiumUiModel) -> Unit,
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

        LiveGameState(
            liveGameState = item.liveGameState,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
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

@Preview
@Composable
private fun LivetalkStadiumItemVerifiedPreview() {
    LivetalkStadiumItem(
        item = LIVETALK_STADIUM_VERIFIED,
        onClick = {},
    )
}

@Preview
@Composable
private fun LivetalkStadiumItemUnVerifiedPreview() {
    LivetalkStadiumItem(
        item = LIVETALK_STADIUM_UNVERIFIED,
        onClick = {},
    )
}

@Preview
@Composable
private fun LivetalkStadiumItemShimmerPreview() {
    ShimmerStadiumItem()
}
