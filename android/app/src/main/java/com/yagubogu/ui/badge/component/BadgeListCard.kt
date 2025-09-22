package com.yagubogu.ui.badge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
fun BadgeListCard(
    badgeList: List<BadgeUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.badge_list_title),
            style = PretendardBold20,
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2열 그리드
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            items(badgeList.size) { index: Int ->
                Badge(badge = badgeList[index])
            }
        }
    }
}

@Preview
@Composable
private fun BadgeListCardPreview() {
    val badge =
        BadgeUiModel(
            imageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
            name = "공포의 주둥아리",
        )
    BadgeListCard(
        badgeList = listOf(badge, badge, badge),
    )
}
