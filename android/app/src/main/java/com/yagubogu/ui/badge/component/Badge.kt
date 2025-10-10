package com.yagubogu.ui.badge.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            BadgeImage(
                badge = badge,
                onLoadSuccess = { isLoading = false },
                onLoadError = { isLoading = false },
                modifier = modifier,
            )

            if (isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
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
