package com.yagubogu.ui.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yagubogu.ui.place.model.PlaceItem
import com.yagubogu.ui.place.model.labelResource
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.categoryThumbnailColor
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_arrow_right
import yagubogu.composeapp.generated.resources.ic_globe_location_pin
import yagubogu.composeapp.generated.resources.ic_marker_pin

@Composable
fun PlaceRecommendationCard(
    item: PlaceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Gray100, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = rememberNoRippleInteractionSource(),
                    indication = null,
                    onClick = onClick,
                ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceThumbnail(item = item)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlaceCategoryLabel(label = stringResource(item.category.labelResource))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.name,
                    style = PretendardBold16,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_marker_pin),
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.distance,
                    style = PretendardRegular12,
                    color = Gray500,
                )
            }
        }
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PlaceThumbnail(
    item: PlaceItem,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(item.category.categoryThumbnailColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_globe_location_pin),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaceCategoryLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = PretendardMedium12,
        color = Gray500,
        modifier =
            modifier
                .background(Gray100, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
