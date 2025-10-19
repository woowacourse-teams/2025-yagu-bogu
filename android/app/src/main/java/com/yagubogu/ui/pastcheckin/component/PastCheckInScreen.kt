package com.yagubogu.ui.pastcheckin.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.ui.component.DefaultDialog
import com.yagubogu.ui.component.Toolbar
import com.yagubogu.ui.pastcheckin.PastCheckInUiEvent
import com.yagubogu.ui.pastcheckin.PastCheckInUiState
import com.yagubogu.ui.pastcheckin.PastCheckInViewModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.YaguBoguTheme
import com.yagubogu.ui.util.formatLocalDate
import java.time.LocalDate

@Composable
fun PastCheckInScreen(
    viewModel: PastCheckInViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    PastCheckInScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onDateSelected = viewModel::onDateSelected,
        onGameSelected = viewModel::onGameSelected,
        modifier = modifier,
    )

    if (uiState.showConfirmDialog) {
        DefaultDialog(
            DefaultDialogUiModel(
                title = "${uiState.selectedGame?.homeTeam?.shortname} vs ${uiState.selectedGame?.awayTeam?.shortname}",
                emoji = "⚾",
                message = "${uiState.selectedDate?.let { formatLocalDate(it) }} ${uiState.selectedGame?.stadiumName}\n과거 직관을 등록하시겠습니까?",
                positiveText = "등록",
                negativeText = "취소",
            ),
            onConfirm = viewModel::confirmRegistration,
            onDismiss = viewModel::dismissDialog,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PastCheckInUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is PastCheckInUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastCheckInScreen(
    uiState: PastCheckInUiState,
    onBackClick: () -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    onGameSelected: (LivetalkStadiumItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            Toolbar(
                title = "과거 직관 등록",
                onBackClick = onBackClick,
            )
        },
        containerColor = Gray050,
        modifier = modifier.background(Gray300),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DateInputField(
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected,
                label = "직관 날짜",
                placeholder = "직관한 경기 날짜를 선택하세요",
            )

            if (uiState.isLoading) {
                InfoPanel(emoji = "", title = "경기 목록을 불러오는 중", showLoading = true)
            }

            if (!uiState.isLoading && uiState.gameList.isNotEmpty()) {
                Text(
                    text = "경기 목록 (${uiState.gameList.size}개)",
                    style = PretendardBold20,
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.gameList) { game ->
                        GameListItem(
                            game = game,
                            onGameClick = onGameSelected,
                        )
                    }
                }
            }

            // 빈 상태 (날짜는 선택했지만 경기가 없는 경우)
            if (!uiState.isLoading &&
                uiState.selectedDate != null &&
                uiState.gameList.isEmpty() &&
                uiState.errorMessage == null
            ) {
                InfoPanel(emoji = "📅", title = "해당 날짜에 경기가 없습니다", subtitle = "다른 날짜를 선택해주세요")
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PastCheckInScreenPreview_Initial() {
    YaguBoguTheme {
        PastCheckInScreen(
            uiState = PastCheckInUiState(),
            onBackClick = { },
            onDateSelected = { },
            onGameSelected = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PastCheckInScreenPreview_Loading() {
    YaguBoguTheme {
        val sampleGames = emptyList<LivetalkStadiumItem>()

        PastCheckInScreen(
            uiState =
                PastCheckInUiState(
                    selectedDate = LocalDate.of(2025, 10, 19),
                    gameList = sampleGames,
                    isLoading = true,
                ),
            onBackClick = { },
            onDateSelected = { },
            onGameSelected = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PastCheckInScreenPreview_WithOutGames() {
    YaguBoguTheme {
        val sampleGames = emptyList<LivetalkStadiumItem>()

        PastCheckInScreen(
            uiState =
                PastCheckInUiState(
                    selectedDate = LocalDate.of(2025, 10, 19),
                    gameList = sampleGames,
                    isLoading = false,
                ),
            onBackClick = { },
            onDateSelected = { },
            onGameSelected = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PastCheckInScreenPreview_WithGames() {
    YaguBoguTheme {
        val sampleGames =
            listOf(
                LivetalkStadiumItem(
                    gameId = 1L,
                    stadiumName = "잠실구장",
                    userCount = 300,
                    awayTeam = Team.LG,
                    homeTeam = Team.HH,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    gameId = 1L,
                    stadiumName = "사직구장",
                    userCount = 300,
                    awayTeam = Team.KT,
                    homeTeam = Team.WO,
                    isVerified = false,
                ),
            )

        PastCheckInScreen(
            uiState =
                PastCheckInUiState(
                    selectedDate = LocalDate.of(2025, 10, 19),
                    gameList = sampleGames,
                    isLoading = false,
                ),
            onBackClick = { },
            onDateSelected = { },
            onGameSelected = { },
        )
    }
}
