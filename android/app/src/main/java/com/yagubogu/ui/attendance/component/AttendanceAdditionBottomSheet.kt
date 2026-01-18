package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.R
import com.yagubogu.presentation.util.DateFormatter
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.attendance.model.PastGameUiState
import com.yagubogu.ui.theme.EsamanruBold32
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium24
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.color
import com.yagubogu.ui.util.shimmerLoading
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAdditionBottomSheet(
    pastGameUiState: PastGameUiState,
    date: LocalDate,
    onPastCheckIn: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        when (pastGameUiState) {
            PastGameUiState.Loading -> PastGameLoadingContent()
            is PastGameUiState.Success -> {
                when (pastGameUiState.pastGames.isNotEmpty()) {
                    true ->
                        PastGamesContent(
                            items = pastGameUiState.pastGames,
                            date = date,
                            onPastCheckIn = onPastCheckIn,
                        )

                    false -> EmptyPastGameContent()
                }
            }
        }
    }
}

@Composable
private fun PastGameLoadingContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(R.string.attendance_history_add_attendance_description),
                style = PretendardSemiBold16,
            )
        }
        items(3) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerLoading(),
            )
        }
    }
}

@Composable
private fun PastGamesContent(
    items: List<PastGameUiModel>,
    date: LocalDate,
    onPastCheckIn: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState: LazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(R.string.attendance_history_add_attendance_description),
                style = PretendardSemiBold16,
            )
        }

        items(
            items = items,
            key = { item: PastGameUiModel -> item.gameId },
        ) { item: PastGameUiModel ->
            PastAttendanceItem(
                item = item,
                date = date,
                onPastCheckIn = onPastCheckIn,
            )
        }
    }
}

@Composable
private fun EmptyPastGameContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.attendance_history_no_game_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.img_baseball_fly_error),
            contentDescription = null,
            modifier =
                Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
        )
    }
}

@Composable
private fun PastAttendanceItem(
    item: PastGameUiModel,
    date: LocalDate,
    onPastCheckIn: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        PastCheckInDialog(
            date = date,
            onConfirm = {
                onPastCheckIn(item.gameId)
                showDialog = false
            },
            onCancel = { showDialog = false },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .clickable { showDialog = true }
                .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.awayTeamName,
                style = EsamanruBold32.copy(color = item.awayTeam.color),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "vs",
                style = PretendardMedium24,
            )
            Text(
                text = item.homeTeamName,
                style = EsamanruBold32.copy(color = item.homeTeam.color),
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${date.format(DateFormatter.yyyyMMdd)} ${item.startAt.format(DateFormatter.hhmm)}",
            style = PretendardRegular12.copy(color = Gray500),
        )
        Text(
            text = item.stadiumName,
            style = PretendardRegular12.copy(color = Gray500),
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingAttendanceAdditionBottomSheetPreview() {
    AttendanceAdditionBottomSheet(
        pastGameUiState = PastGameUiState.Loading,
        date = LocalDate.now(),
        onPastCheckIn = {},
        onDismiss = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    )
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceAdditionBottomSheetPreview() {
    AttendanceAdditionBottomSheet(
        pastGameUiState = PastGameUiState.Success(PAST_GAME_UI_MODELS),
        date = LocalDate.now(),
        onPastCheckIn = {},
        onDismiss = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    )
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyAttendanceAdditionBottomSheetPreview() {
    AttendanceAdditionBottomSheet(
        pastGameUiState = PastGameUiState.Success(emptyList()),
        date = LocalDate.now(),
        onPastCheckIn = {},
        onDismiss = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    )
}

@Preview
@Composable
private fun PastAttendanceItemPreview() {
    PastAttendanceItem(
        item = PAST_GAME_UI_MODELS[0],
        date = LocalDate.now(),
        onPastCheckIn = { },
    )
}
