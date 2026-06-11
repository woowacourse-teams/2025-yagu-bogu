package com.yagubogu.ui.attendance.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.yagubogu.ui.attendance.component.ATTENDANCE_HISTORY_ITEM_PLAYED
import com.yagubogu.ui.attendance.detail.component.AttendanceDetailTabRow
import com.yagubogu.ui.attendance.detail.component.DeleteDiaryDialog
import com.yagubogu.ui.attendance.detail.component.ExitDiaryDialog
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailShareUiState
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailTab
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailUiEvent
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import com.yagubogu.ui.attendance.detail.model.PLAYER_RECORD
import com.yagubogu.ui.attendance.detail.model.PlayerRecordUiModel
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.main.component.LoadingOverlay
import com.yagubogu.ui.share.AttendanceTicketCaptureLayer
import com.yagubogu.ui.share.rememberImageSharer
import com.yagubogu.ui.share.shareAttendanceTicketImage
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.showSingleSnackbar
import com.yagubogu.ui.util.yyyyMMddDayOfWeekFormatter
import kotlinx.datetime.format
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_delete
import yagubogu.composeapp.generated.resources.ic_share
import yagubogu.composeapp.generated.resources.ic_trash

private val shareLogger = Logger.withTag("AttendanceDetailShare")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailScreen(
    item: AttendanceHistoryItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceDetailViewModel = koinViewModel(parameters = { parametersOf(item.id) }),
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shareRequestKey: Int by remember(item.id) { mutableStateOf(0) }
    var isCaptureLayerVisible: Boolean by remember(item.id) { mutableStateOf(false) }

    val snackbarState: SnackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val imageSharer = rememberImageSharer()
    val graphicsLayer = rememberGraphicsLayer()
    val pagerState = rememberPagerState { AttendanceDetailTab.entries.size }

    val playerRecordUiModel: PlayerRecordUiModel by viewModel.playerRecordUiModel.collectAsStateWithLifecycle()
    val attendanceDetailDiaryUiState: AttendanceDetailDiaryUiState by viewModel.attendanceDetailDiaryUiState.collectAsStateWithLifecycle()
    val shareUiState: AttendanceDetailShareUiState by viewModel.shareUiState.collectAsStateWithLifecycle()
    val isShareEnabled = shareUiState.isLoaded && !isCaptureLayerVisible

    val isWriting =
        pagerState.currentPage == AttendanceDetailTab.DIARY.ordinal && attendanceDetailDiaryUiState.mode == DiaryMode.WRITE

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AttendanceDetailUiEvent.ShowSnackbar ->
                    snackbarState.showSingleSnackbar(
                        scope = this,
                        stringResource = event.message,
                    )

                AttendanceDetailUiEvent.DeleteSuccess -> onBackClick()
            }
        }
    }

    LaunchedEffect(item.id) {
        viewModel.loadShareData(year = item.dateTime.year)
    }

    LaunchedEffect(shareRequestKey) {
        if (shareRequestKey == 0) return@LaunchedEffect

        withFrameNanos { }

        try {
            shareAttendanceTicketImage(
                graphicsLayer = graphicsLayer,
                imageSharer = imageSharer,
                checkInId = item.id,
            )
        } catch (exception: Exception) {
            shareLogger.w(exception) { "Ticket capture failed" }
        } finally {
            isCaptureLayerVisible = false
        }
    }

    AttendanceDetailScreen(
        item = item,
        playerRecordUiModel = playerRecordUiModel,
        attendanceDetailDiaryUiState = attendanceDetailDiaryUiState,
        date = item.dateTime.date.format(yyyyMMddDayOfWeekFormatter),
        pagerState = pagerState,
        isShareEnabled = isShareEnabled,
        onBackClick = {
            when {
                attendanceDetailDiaryUiState.isLoading -> Unit
                isWriting -> showExitDialog = true
                else -> onBackClick()
            }
        },
        onDeleteClick = { if (!attendanceDetailDiaryUiState.isLoading) showDeleteDialog = true },
        onImagesSelected = viewModel::addImages,
        onImageDeleted = viewModel::deleteImage,
        onEditClick = viewModel::editDiary,
        onSaveClick = viewModel::saveDiary,
        onImagePickerError = { message -> snackbarState.showSingleSnackbar(scope, message) },
        onShareClick = {
            isCaptureLayerVisible = true
            shareRequestKey++
        },
        modifier = modifier,
    )

    if (isCaptureLayerVisible) {
        shareUiState.shareData?.let { shareData ->
            AttendanceTicketCaptureLayer(
                item = item,
                shareData = shareData,
                graphicsLayer = graphicsLayer,
            )
        }
    }

    if (showExitDialog) {
        ExitDiaryDialog(
            onConfirm = onBackClick,
            onCancel = { showExitDialog = false },
        )
    }

    if (showDeleteDialog) {
        DeleteDiaryDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteCheckIn()
            },
            onCancel = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun AttendanceDetailScreen(
    item: AttendanceHistoryItem,
    playerRecordUiModel: PlayerRecordUiModel,
    attendanceDetailDiaryUiState: AttendanceDetailDiaryUiState,
    pagerState: PagerState,
    date: String,
    isShareEnabled: Boolean,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onImagesSelected: (images: List<String>) -> Unit,
    onImageDeleted: (index: Int) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: (comment: String) -> Unit,
    onImagePickerError: (message: StringResource) -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Gray050),
        ) {
            AttendanceDetailToolbar(
                date = date,
                onBackClick = onBackClick,
                onDeleteClick = onDeleteClick,
                isShareEnabled = isShareEnabled,
                onShareClick = onShareClick,
            )
            if (!isKeyboardVisible) {
                AttendanceDetailTabRow(pagerState)
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = AttendanceDetailTab.entries.size,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    AttendanceDetailTab.GAME_RECORD.ordinal ->
                        AttendanceDetailGameRecordScreen(
                            item = item,
                            playerRecord = playerRecordUiModel,
                        )

                    AttendanceDetailTab.DIARY.ordinal ->
                        AttendanceDetailDiaryScreen(
                            uiState = attendanceDetailDiaryUiState,
                            onImagesSelected = onImagesSelected,
                            onImageDeleted = onImageDeleted,
                            onEditClick = onEditClick,
                            onSaveClick = onSaveClick,
                            onImagePickerError = onImagePickerError,
                        )
                }
            }
        }
        LoadingOverlay(isLoading = attendanceDetailDiaryUiState.isLoading)
    }
}

@Composable
private fun AttendanceDetailToolbar(
    date: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isShareEnabled: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultToolbar(
        onBackClick = onBackClick,
        modifier = modifier,
        title = date,
        actions = {
            IconButton(
                enabled = isShareEnabled,
                onClick = onShareClick,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_share),
                    contentDescription = null,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_trash),
                    contentDescription = stringResource(Res.string.attendance_detail_delete),
                )
            }
        },
    )
}

@Preview
@Composable
private fun AttendanceDetailScreenDiaryTabPreview() {
    AttendanceDetailScreen(
        item = ATTENDANCE_HISTORY_ITEM_PLAYED,
        playerRecordUiModel = PLAYER_RECORD,
        attendanceDetailDiaryUiState = AttendanceDetailDiaryUiState(),
        pagerState = rememberPagerState(AttendanceDetailTab.DIARY.ordinal) { AttendanceDetailTab.entries.size },
        date = "2025.08.14 (목)",
        isShareEnabled = true,
        onBackClick = {},
        onDeleteClick = {},
        onImagesSelected = {},
        onImageDeleted = {},
        onEditClick = {},
        onSaveClick = { _ -> },
        onImagePickerError = {},
        onShareClick = {},
    )
}
