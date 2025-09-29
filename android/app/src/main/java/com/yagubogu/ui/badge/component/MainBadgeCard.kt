package com.yagubogu.ui.badge.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.badge.model.BADGE_ID_0_ACQUIRED_FIXTURE_
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.PretendardBold20

@Composable
fun MainBadgeCard(
    badge: BadgeUiModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.badge_main_badge_title),
            style = PretendardBold20,
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (badge == null) {
            EmptyBadge(
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Badge(
                badge = badge,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PaymentCardPreview(
    @PreviewParameter(MainBadgeCardPreviewParameterProvider::class) badge: BadgeUiModel?,
) {
    MainBadgeCard(badge = badge)
}

private class MainBadgeCardPreviewParameterProvider : PreviewParameterProvider<BadgeUiModel?> {
    override val values: Sequence<BadgeUiModel?> =
        sequenceOf(
            null,
            BADGE_ID_0_ACQUIRED_FIXTURE_.badge,
        )
}
