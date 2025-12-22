package com.yagubogu.ui.livetalk

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.R
import com.yagubogu.presentation.livetalk.chat.LivetalkChatActivity
import com.yagubogu.ui.livetalk.component.LIVETALK_STADIUM_ITEMS
import com.yagubogu.ui.livetalk.component.LivetalkStadiumItem
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.util.BackPressHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LivetalkScreen(
    snackbarHostState: SnackbarHostState,
    scrollToTopEvent: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
    viewModel: LivetalkViewModel = hiltViewModel(),
) {
    val livetalkStadiumItems: List<LivetalkStadiumItem> by viewModel.stadiumItems.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchGames()
    }

    BackPressHandler(snackbarHostState, coroutineScope)

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
                scrollToTopEvent = scrollToTopEvent,
            )

        false -> EmptyLivetalkScreen(modifier = modifier)
    }
}

@Composable
private fun LivetalkScreen(
    items: List<LivetalkStadiumItem>,
    onItemClick: (LivetalkStadiumItem) -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val lazyListState: LazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            lazyListState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding =
            PaddingValues(
                top = 8.dp,
                bottom = 20.dp,
                start = 20.dp,
                end = 20.dp,
            ),
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        items(
            count = items.size,
            key = { index: Int -> items[index].gameId },
        ) { index: Int ->
            val item: LivetalkStadiumItem = items[index]
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
