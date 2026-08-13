package com.yagubogu.ui.badge.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yagubogu.ui.badge.model.BADGE_ID_0_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeUiModel
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.badge_image_description
import yagubogu.composeapp.generated.resources.img_badge_lock

@Composable
fun BadgeImage(
    badge: BadgeUiModel,
    onLoadSuccess: () -> Unit,
    onLoadError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model =
            if (LocalInspectionMode.current) {
                Res.drawable.img_badge_lock
            } else {
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(badge.imageUrl)
                    .crossfade(true)
                    .build()
            },
        contentDescription = stringResource(Res.string.badge_image_description),
        colorFilter =
            if (!badge.isAcquired) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) })
            } else {
                null
            },
        onSuccess = { onLoadSuccess() },
        onError = { onLoadError() },
        modifier = modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
private fun BadgeImagePreview() {
    BadgeImage(
        badge = BADGE_ID_0_ACQUIRED_FIXTURE.badge,
        onLoadSuccess = {},
        onLoadError = {},
    )
}
