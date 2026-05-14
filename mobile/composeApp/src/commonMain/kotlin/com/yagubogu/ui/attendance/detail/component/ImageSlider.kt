package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_image_content_description

@Composable
fun ImageSlider(
    images: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onImageClick: ((index: Int) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = images.size,
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription =
                    stringResource(
                        Res.string.attendance_detail_image_content_description,
                        page,
                    ),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .aspectRatio(1f)
                        .dropShadow(
                            shape = RoundedCornerShape(12.dp),
                            shadow =
                                Shadow(
                                    radius = 4.dp,
                                    offset = DpOffset(x = 0.dp, 4.dp),
                                    alpha = 0.25f,
                                ),
                        ).clip(RoundedCornerShape(12.dp))
                        .background(color = White)
                        .clickable(enabled = onImageClick != null) { onImageClick?.invoke(page) },
            )
        }
        SliderDots(size = images.size, selectedIndex = pagerState.currentPage)
    }
}

@Composable
fun SliderDots(
    size: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(size) { idx: Int ->
            Box(
                modifier =
                    Modifier
                        .size(6.dp)
                        .background(
                            shape = CircleShape,
                            color = if (idx == selectedIndex) Primary500 else Gray300,
                        ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageSliderPreview() {
    Surface(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        ImageSlider(
            images = persistentListOf("", "", ""),
        )
    }
}
