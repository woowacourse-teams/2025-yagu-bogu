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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.badge.model.BADGE_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.theme.Gray300
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
    badge: BadgeUiModel,
    onRegisterClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Badge(badge = badge)
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text(text = "${badge.achievedRate}", style = PretendardBold12, color = Primary700)
                Text(
                    text = stringResource(R.string.badge_achieved_rate_message),
                    style = PretendardRegular12,
                    color = Primary700,
                )
            }
            HorizontalDivider(
                thickness = 0.4.dp,
                color = Gray300,
                modifier = Modifier.padding(vertical = 20.dp),
            )
            Text(
                text = badge.description,
                style = PretendardMedium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(30.dp))
            if (badge.isAcquired) {
                Button(
                    onClick = onRegisterClick,
                    colors =
                        ButtonColors(
                            containerColor = Primary500,
                            contentColor = White,
                            disabledContainerColor = Primary500,
                            disabledContentColor = White,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.badge_register_main_badge),
                        style = PretendardBold16,
                    )
                }
                Text(
                    text =
                        stringResource(
                            R.string.badge_achieved_date,
                            badge.achievedAt.format(DateFormatter.yyyyMMdd),
                        ),
                    style = PretendardRegular12,
                    color = Gray500,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LinearProgressIndicator(
                    progress = { badge.progressRate.toFloat() / 100 },
                    color = Primary500,
                    trackColor = Gray300,
                    gapSize = (-12).dp,
                    drawStopIndicator = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                )
                Text(
                    text =
                        stringResource(
                            R.string.badge_progress_rate,
                            badge.progressRate,
                        ),
                    style = PretendardRegular12,
                    color = Gray500,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BadgeBottomSheetPreview() {
    BadgeBottomSheet(
        badge = BADGE_NOT_ACQUIRED_FIXTURE,
        onRegisterClick = {},
        onDismiss = {},
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    )
}
