package com.yagubogu.ui.badge.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yagubogu.R
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.util.noRippleClickable
import com.yagubogu.ui.util.shimmerLoading

@Composable
fun Badge(
    badge: BadgeUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.noRippleClickable(onClick),
    ) {
        var isLoading by remember { mutableStateOf(true) }

        Box(
            modifier =
                Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model =
                    if (LocalInspectionMode.current) {
                        R.drawable.img_badge_lock
                    } else {
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(badge.imageUrl)
                            .crossfade(true)
                            .build()
                    },
                contentDescription = stringResource(R.string.badge_image_description),
                colorFilter =
                    if (!badge.isAcquired) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) })
                    } else {
                        null
                    },
                onSuccess = { isLoading = false },
                onError = { isLoading = false },
                modifier = Modifier.matchParentSize(),
            )

            if (isLoading) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .shimmerLoading(),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = badge.name,
            style = PretendardSemiBold,
            fontSize = 14.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgePreview() {
    Badge(badge = BadgeUiModel(0, "공포의 주둥아리", "", true))
}
