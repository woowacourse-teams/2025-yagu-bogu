package com.yagubogu.ui.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.painterResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_marker_pin

@Composable
fun PlaceStadiumSelector(
    stadiumName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Gray200, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = rememberNoRippleInteractionSource(),
                    indication = null,
                    onClick = onClick,
                ).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .background(Primary050, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_marker_pin),
                contentDescription = null,
                tint = Primary500,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stadiumName,
            style = PretendardBold20,
            color = Gray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(24.dp),
        )
    }
}
