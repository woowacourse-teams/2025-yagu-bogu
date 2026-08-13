package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.util.shimmerLoading

@Composable
fun LivetalkChatBubbleListShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = false,
    ) {
        items(10) { index ->
            val isMyBubble = index % 3 == 0

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(if (index % 2 == 0) 90.dp else 70.dp)
                        .padding(
                            start = if (isMyBubble) 30.dp else 0.dp,
                            end = if (isMyBubble) 0.dp else 30.dp,
                        ).clip(RoundedCornerShape(12.dp))
                        .shimmerLoading(),
            )
        }
    }
}

@Preview
@Composable
private fun LivetalkChatBubbleListPreview() {
    LivetalkChatBubbleListShimmer()
}
