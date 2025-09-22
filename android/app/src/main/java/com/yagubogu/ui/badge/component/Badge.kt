package com.yagubogu.ui.badge.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yagubogu.R
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.PretendardSemiBold

@Composable
fun Badge(
    badge: BadgeUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = badge.imageUrl,
            contentDescription = stringResource(R.string.badge_image_description),
            placeholder = painterResource(R.drawable.img_badge_lock),
            modifier = Modifier.size(120.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = badge.name,
            style = PretendardSemiBold,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun Badge(
    @DrawableRes image: Int,
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = stringResource(R.string.badge_image_description),
            modifier = Modifier.size(120.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = PretendardSemiBold,
            fontSize = 14.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgePreview() {
    Badge(image = R.drawable.img_badge_lock, name = "공포의 주둥아리")
}
