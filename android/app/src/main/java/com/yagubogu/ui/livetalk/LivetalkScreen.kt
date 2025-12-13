package com.yagubogu.ui.livetalk

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.livetalk.component.LivetalkStadiumItem
import com.yagubogu.ui.theme.Gray050

@Composable
fun LivetalkScreen(
//    viewModel: LivetalkViewModel,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 20.dp),
    ) {
        List(5) { LivetalkStadiumItem(isVerified = true) }
    }
}

@Preview
@Composable
private fun LivetalkScreenPreview() {
    LivetalkScreen()
}
