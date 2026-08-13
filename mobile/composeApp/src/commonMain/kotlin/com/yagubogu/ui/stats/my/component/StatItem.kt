package com.yagubogu.ui.stats.my.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.util.shimmerLoading
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.stats_no_data

@Composable
fun StatItem(
    title: String,
    value: StatItemValue,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        when (value) {
            StatItemValue.Loading -> {
                Text(
                    text = "로딩",
                    style = PretendardSemiBold20,
                    modifier = Modifier.shimmerLoading(roundCorner = 12.dp),
                )
            }
            is StatItemValue.Data -> {
                Text(
                    text = value.text,
                    style = PretendardSemiBold20,
                )
            }
            StatItemValue.NoData -> {
                Text(
                    text = stringResource(Res.string.stats_no_data),
                    style = PretendardSemiBold20,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = PretendardRegular12, color = Gray500)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatItemAllStatesPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp), // 아이템 간 간격
    ) {
        // 1. 데이터가 있는 경우
        StatItem(
            title = "행운의 구장",
            value = StatItemValue.Data("고척"),
            emoji = "🍀",
        )

        // 2. 데이터가 없는 경우
        StatItem(
            title = "행운의 구장",
            value = StatItemValue.NoData,
            emoji = "🍀",
        )

        // 3. 로딩 중인 경우
        StatItem(
            title = "행운의 구장",
            value = StatItemValue.Loading,
            emoji = "🍀",
        )
    }
}
