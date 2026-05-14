package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailTab
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.Primary050
import com.yagubogu.ui.theme.Primary100
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.Primary700
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AttendanceDetailTabRow(pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier =
            Modifier
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Primary100, RoundedCornerShape(12.dp)),
        containerColor = Primary050,
        contentColor = Primary500,
        indicator = {
            Box(
                modifier =
                    Modifier
                        .tabIndicatorOffset(
                            selectedTabIndex = pagerState.currentPage,
                            matchContentSize = false,
                        ).padding(horizontal = 4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary500)
                        .zIndex(-1f),
            )
        },
        divider = {},
        tabs = {
            AttendanceDetailTab.entries.forEachIndexed { index, tab ->
                val isSelected = pagerState.currentPage == index

                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    interactionSource = rememberNoRippleInteractionSource(),
                    content = {
                        val style: TextStyle =
                            if (isSelected) {
                                PretendardSemiBold.copy(color = White, fontSize = 18.sp)
                            } else {
                                PretendardSemiBold16.copy(color = Primary700)
                            }
                        Text(
                            text = stringResource(tab.titleRes),
                            style = style,
                            modifier = Modifier.padding(12.dp),
                        )
                    },
                )
            }
        },
    )
}

@Preview
@Composable
private fun AttendanceDetailTabRowPreview() {
    Surface(
        modifier = Modifier.background(Gray050),
    ) {
        AttendanceDetailTabRow(pagerState = rememberPagerState(pageCount = { 0 }))
    }
}
