package com.yagubogu.ui.badge.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.badge.model.BADGE_ID_0_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold12
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeBottomSheet(
    badgeInfo: BadgeInfoUiModel,
    isRepresentativeBadge: Boolean,
    onRegisterClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Badge(badge = badgeInfo.badge)
            Spacer(modifier = Modifier.height(10.dp))
            BadgeDescriptionHeader(badgeInfo)
            Spacer(modifier = Modifier.height(30.dp))
            when (badgeInfo.badge.isAcquired) {
                true -> AcquiredBadgeContent(onRegisterClick, badgeInfo, isRepresentativeBadge)
                false -> UnacquiredBadgeContent(badgeInfo)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BadgeDescriptionHeader(
    badgeInfo: BadgeInfoUiModel,
    modifier: Modifier = Modifier,
) {
    Row {
        Text(
            text = "${badgeInfo.achievedRate}",
            style = PretendardBold12,
            color = Primary700,
        )
        Text(
            text = stringResource(R.string.badge_achieved_rate_message),
            style = PretendardRegular12,
            color = Primary700,
        )
    }
    HorizontalDivider(
        thickness = 0.4.dp,
        color = Gray300,
        modifier = modifier.padding(vertical = 20.dp),
    )
    Text(
        text = badgeInfo.description,
        style = PretendardMedium,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun AcquiredBadgeContent(
    onRegisterClick: (Long) -> Unit,
    badgeInfo: BadgeInfoUiModel,
    isRepresentativeBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onRegisterClick(badgeInfo.badge.id) },
        enabled = !isRepresentativeBadge,
        colors =
            ButtonColors(
                containerColor = Primary500,
                contentColor = White,
                disabledContainerColor = Gray400,
                disabledContentColor = White,
            ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text =
                stringResource(
                    if (isRepresentativeBadge) {
                        R.string.badge_already_used_badge
                    } else {
                        R.string.badge_register_main_badge
                    },
                ),
            style = PretendardBold16,
        )
    }
    Text(
        text =
            stringResource(
                R.string.badge_achieved_date,
                badgeInfo.achievedAt?.format(DateFormatter.yyyyMMdd) ?: "",
            ),
        style = PretendardRegular12,
        color = Gray500,
        modifier = modifier.padding(top = 12.dp),
    )
}

@Composable
private fun UnacquiredBadgeContent(
    badgeInfo: BadgeInfoUiModel,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { (badgeInfo.progressRate / 100).toFloat() },
        color = Primary500,
        trackColor = Gray300,
        gapSize = (-12).dp,
        drawStopIndicator = {},
        modifier =
            modifier
                .fillMaxWidth()
                .height(12.dp),
    )
    Text(
        text =
            stringResource(
                R.string.badge_progress_rate,
                badgeInfo.progressRate,
            ),
        style = PretendardRegular12,
        color = Gray500,
        modifier = modifier.padding(top = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BadgeBottomSheetPreview() {
    BadgeBottomSheet(
        badgeInfo = BADGE_ID_0_NOT_ACQUIRED_FIXTURE,
        isRepresentativeBadge = true,
        onRegisterClick = {},
        onDismiss = {},
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    )
}
