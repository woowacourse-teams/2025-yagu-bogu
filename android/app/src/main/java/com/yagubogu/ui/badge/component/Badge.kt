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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yagubogu.R
import com.yagubogu.ui.theme.PretendardSemiBold

@Composable
fun Badge(
    imageUrl: String,
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
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

@Composable
fun Badge(
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
            contentDescription = null,
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
    Badge(image = R.drawable.img_badge_princess, name = "공포의 주둥아리")
}
