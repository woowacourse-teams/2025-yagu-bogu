package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.util.shimmerLoading

@Composable
fun LivetalkChatScreenTitle(
    stadiumName: String?,
    matchText: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            stadiumName.isNullOrEmpty() || matchText.isNullOrEmpty() -> {
                Box(
                    modifier =
                        Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .shimmerLoading(),
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier =
                        Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .shimmerLoading(),
                )
            }
            else -> {
                Text(
                    text = stadiumName,
                    style = PretendardBold20,
                )
                Text(
                    text = matchText,
                    style = PretendardRegular12,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LivetalkChatScreenTitlePreview() {
    LivetalkChatScreenTitle("고척 스카이돔", "두산 vs 키움")
}

@Preview
@Composable
private fun LivetalkChatScreenTitleShimmerPreview() {
    LivetalkChatScreenTitle(null, null)
}
