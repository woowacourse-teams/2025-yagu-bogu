package com.yagubogu.ui.livetalk.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
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
import com.yagubogu.ui.util.emoji
import com.yagubogu.ui.util.noRippleClickable

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
                )
                .noRippleClickable { onClick(item) }
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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_users),
                    contentDescription = stringResource(R.string.livetalk_user_icon_description),
                    tint = Gray500,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = item.userCount.toString(),
                    style = PretendardMedium12.copy(color = Gray500),
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = stringResource(R.string.livetalk_stadium_select_arrow_description),
                tint = Gray500,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamItem(
                name = item.awayTeam.shortname,
                emoji = item.awayTeam.emoji,
                teamColor = item.awayTeam.color,
                modifier = Modifier.weight(1.0f),
            )
            Text(
                text = "vs",
                style = PretendardMedium.copy(fontSize = 20.dpToSp, color = Gray500),
            )
            TeamItem(
                name = item.homeTeam.shortname,
                emoji = item.homeTeam.emoji,
                teamColor = item.homeTeam.color,
                modifier = Modifier.weight(1.0f),
            )
        }
    }
}

@Composable
private fun TeamItem(
    name: String,
    emoji: String,
    teamColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = emoji,
            style = PretendardMedium.copy(fontSize = 28.sp),
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
        item = LIVETALK_STADIUM_ITEM_UNVERIFIED,
        onClick = {},
    )
}
