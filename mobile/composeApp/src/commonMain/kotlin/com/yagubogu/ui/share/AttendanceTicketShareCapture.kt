package com.yagubogu.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem

private const val CAPTURE_LAYER_OFFSCREEN_Y = 10000

@Composable
fun AttendanceTicketCaptureLayer(
    item: AttendanceHistoryItem,
    shareData: AttendanceTicketShareData,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val widthDp = with(density) { ShareTicketSpec.CANVAS_WIDTH_PX.toDp() }
    val heightDp = with(density) { ShareTicketSpec.CANVAS_HEIGHT_PX.toDp() }

    Box(
        modifier =
            modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(width = 0, height = 0) {
                        placeable.placeRelative(x = 0, y = CAPTURE_LAYER_OFFSCREEN_Y)
                    }
                },
    ) {
        Box(
            modifier =
                Modifier
                    .requiredSize(widthDp, heightDp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                    },
        ) {
            AttendanceTicketImageContent(
                item = item,
                shareData = shareData,
            )
        }
    }
}

suspend fun shareAttendanceTicketImage(
    graphicsLayer: GraphicsLayer,
    imageSharer: ImageSharer,
    checkInId: Long,
) {
    val imageBitmap = graphicsLayer.toImageBitmap()
    val imageBytes = imageBitmap.toByteArray()
    imageSharer.shareImage(
        imageBytes = imageBytes,
        fileName = "attendance_ticket_$checkInId.png",
        mimeType = "image/png",
    )
}
