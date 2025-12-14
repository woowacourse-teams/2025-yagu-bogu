package com.yagubogu.ui.livetalk

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.presentation.livetalk.LivetalkViewModel
import com.yagubogu.presentation.livetalk.chat.LivetalkChatActivity
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.component.LIVETALK_STADIUM_ITEMS
import com.yagubogu.ui.livetalk.component.LivetalkStadiumItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium

@Composable
fun LivetalkScreen(
    viewModel: LivetalkViewModel,
    modifier: Modifier = Modifier,
) {
    val livetalkStadiumItems: List<LivetalkStadiumItem> by viewModel.stadiumItems.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    when (livetalkStadiumItems.isNotEmpty()) {
        true ->
            LivetalkScreen(
                items = livetalkStadiumItems,
                onItemClick = { item: LivetalkStadiumItem ->
                    val intent =
                        LivetalkChatActivity.newIntent(context, item.gameId, item.isVerified)
                    context.startActivity(intent)
                },
                modifier = modifier,
                scrollState = scrollState,
            )

        false -> EmptyLivetalkScreen(modifier = modifier)
    }
}

@Composable
private fun LivetalkScreen(
    items: List<LivetalkStadiumItem>,
    onItemClick: (LivetalkStadiumItem) -> Unit,
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
            LivetalkStadiumItem(
                item = item,
                onClick = onItemClick,
            )
        }
    }
}

@Composable
private fun EmptyLivetalkScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_baseball_fly_error),
            contentDescription = stringResource(R.string.livetalk_empty_game_illustration_description),
            modifier =
                Modifier
                    .height(250.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.livetalk_empty_game_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
    }
}

@Preview("현장톡 화면")
@Composable
private fun LivetalkScreenPreview() {
    LivetalkScreen(
        items = LIVETALK_STADIUM_ITEMS,
        onItemClick = {},
    )
}

@Preview("빈 현장톡 화면")
@Composable
private fun EmptyLivetalkScreenPreview() {
    EmptyLivetalkScreen()
}
