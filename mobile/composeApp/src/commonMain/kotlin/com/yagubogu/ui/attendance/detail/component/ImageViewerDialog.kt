package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.White
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_close
import yagubogu.composeapp.generated.resources.attendance_detail_image_content_description
import yagubogu.composeapp.generated.resources.ic_close

@Composable
fun ImageViewerDialog(
    images: ImmutableList<String>,
    initialPage: Int,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.9f)),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val zoomState = rememberZoomState()
                AsyncImage(
                    model = images[page],
                    contentDescription =
                        stringResource(
                            Res.string.attendance_detail_image_content_description,
                            page,
                        ),
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .zoomable(
                                zoomState = zoomState,
                                scrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
                            ),
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Black.copy(alpha = 0.4f), CircleShape)
                        .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(Res.string.all_close),
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (images.size > 1) {
                SliderDots(
                    size = images.size,
                    selectedIndex = pagerState.currentPage,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ImageViewerDialogPreview() {
    ImageViewerDialog(
        images = persistentListOf(),
        initialPage = 0,
        onDismiss = {},
    )
}
