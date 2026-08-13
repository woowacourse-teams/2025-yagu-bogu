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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.yagubogu.ui.util.hhmmFormatter
import com.yagubogu.ui.util.shimmerLoading
import com.yagubogu.ui.util.yyyyMMddFormatter
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_history_add_attendance_description
import yagubogu.composeapp.generated.resources.attendance_history_no_game_description
import yagubogu.composeapp.generated.resources.img_baseball_fly_error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAdditionBottomSheet(
    pastGameUiState: PastGameUiState,
    onPastCheckIn: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        val lazyListState: LazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (pastGameUiState) {
                PastGameUiState.Loading -> {
                    item { AttendanceHeader() }
                    items(3) { PastGameLoadingItem() }
                }

                is PastGameUiState.Success -> {
                    when (pastGameUiState.pastGames.isNotEmpty()) {
                        true -> {
                            item { AttendanceHeader() }
                            items(
                                items = pastGameUiState.pastGames,
                                key = { item: PastGameUiModel -> item.gameId },
                            ) { item: PastGameUiModel ->
                                PastGameItem(
                                    item = item,
                                    onPastCheckIn = onPastCheckIn,
                                )
                            }
                        }
                        false -> item { EmptyPastGameContent() }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceHeader() {
    Text(
        text = stringResource(Res.string.attendance_history_add_attendance_description),
        style = PretendardSemiBold16,
    )
}

@Composable
private fun PastGameLoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerLoading(),
    )
}

@Composable
private fun PastGameItem(
    item: PastGameUiModel,
    onPastCheckIn: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        PastCheckInDialog(
            date = item.date,
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
            text = "${item.date.format(yyyyMMddFormatter)} ${item.startAt.format(hhmmFormatter)}",
            style = PretendardRegular12.copy(color = Gray500),
        )
        Text(
            text = item.stadiumName,
            style = PretendardRegular12.copy(color = Gray500),
        )
    }
}

@Composable
private fun EmptyPastGameContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.attendance_history_no_game_description),
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(Res.drawable.img_baseball_fly_error),
            contentDescription = null,
            modifier =
                Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingAttendanceAdditionBottomSheetPreview() {
    AttendanceAdditionBottomSheet(
        pastGameUiState = PastGameUiState.Loading,
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
        onPastCheckIn = {},
        onDismiss = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    )
}

@Preview
@Composable
private fun PastGameItemPreview() {
    PastGameItem(
        item = PAST_GAME_UI_MODELS[0],
        onPastCheckIn = { },
    )
}
