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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.presentation.livetalk.LivetalkViewModel
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.component.LIVETALK_STADIUM_ITEMS
import com.yagubogu.ui.livetalk.component.LivetalkStadiumItem
import com.yagubogu.ui.theme.Gray050

@Composable
fun LivetalkScreen(
    viewModel: LivetalkViewModel,
    modifier: Modifier = Modifier,
) {
    val livetalkStadiumItems: List<LivetalkStadiumItem> by viewModel.livetalkStadiumItems.collectAsStateWithLifecycle()
    val scrollState: ScrollState = rememberScrollState()

    LivetalkScreen(
        items = livetalkStadiumItems,
        modifier = modifier,
        scrollState = scrollState,
    )
}

@Composable
private fun LivetalkScreen(
    items: List<LivetalkStadiumItem>,
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
        items.forEach { item: LivetalkStadiumItem ->
            LivetalkStadiumItem(item = item, onClick = {})
        }
    }
}

@Preview
@Composable
private fun LivetalkScreenPreview() {
    LivetalkScreen(
        items = LIVETALK_STADIUM_ITEMS,
    )
}
