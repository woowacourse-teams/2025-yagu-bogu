package com.yagubogu.ui.attendance.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.share.AttendanceTicketCaptureLayer
import com.yagubogu.ui.share.AttendanceTicketShareData
import com.yagubogu.ui.share.ImageSharer
import com.yagubogu.ui.share.LoadAttendanceTicketShareDataUseCase
import com.yagubogu.ui.share.rememberImageSharer
import com.yagubogu.ui.share.shareAttendanceTicketImage
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray800
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.YaguBoguTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_history_share
import yagubogu.composeapp.generated.resources.ic_share

private val shareLogger = Logger.withTag("Share")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceShareBottomSheet(
    item: AttendanceHistoryItem,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    loadAttendanceTicketShareDataUseCase: LoadAttendanceTicketShareDataUseCase = koinInject(),
) {
    val imageSharer: ImageSharer = rememberImageSharer()
    val scope: CoroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var shareData: AttendanceTicketShareData? by remember(item.id) { mutableStateOf(null) }
    var isShareDataLoaded: Boolean by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(item.id) {
        isShareDataLoaded = false
        shareData = null
        loadAttendanceTicketShareDataUseCase(item.dateTime.year)
            .onSuccess { loadedShareData ->
                shareData = loadedShareData
                isShareDataLoaded = loadedShareData.isReady
            }.onFailure { exception ->
                shareLogger.w(exception) { "공유용 직관 데이터 조회 실패" }
            }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Gray050,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AttendanceShareBottomSheetContent(
                isShareEnabled = isShareDataLoaded,
                onShareClick = {
                    scope.launch {
                        try {
                            shareAttendanceTicketImage(
                                graphicsLayer = graphicsLayer,
                                imageSharer = imageSharer,
                                checkInId = item.id,
                            )
                        } catch (e: Exception) {
                            shareLogger.w(e) { "Ticket capture failed" }
                        }
                        onShareClick()
                    }
                },
            )

            shareData?.let { loadedShareData ->
                AttendanceTicketCaptureLayer(
                    item = item,
                    shareData = loadedShareData,
                    graphicsLayer = graphicsLayer,
                )
            }
        }
    }
}

@Composable
private fun AttendanceShareBottomSheetContent(
    isShareEnabled: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
    ) {
        ShareMenuItem(
            onClick = onShareClick,
            enabled = isShareEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ShareMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (enabled) 1f else 0.45f

    Row(
        modifier =
            modifier
                .alpha(contentAlpha)
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                ).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_share),
            contentDescription = null,
            tint = Gray800,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(Res.string.attendance_history_share),
            style = PretendardMedium.copy(fontSize = 18.sp),
            color = Gray800,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceShareBottomSheetContentPreview() {
    YaguBoguTheme {
        AttendanceShareBottomSheetContent(
            isShareEnabled = true,
            onShareClick = {},
        )
    }
}
