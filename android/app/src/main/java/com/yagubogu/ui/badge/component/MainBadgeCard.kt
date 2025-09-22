package com.yagubogu.ui.badge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.White

@Composable
fun MainBadgeCard(
    badge: BadgeUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.badge_main_badge_title),
            style = PretendardBold20,
        )
        Badge(
            badge = badge,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun MainBadgeCardPreview() {
    MainBadgeCard(
        badge =
            BadgeUiModel(
                imageUrl = "",
                name = "공포의 주둥아리",
            ),
    )
}
