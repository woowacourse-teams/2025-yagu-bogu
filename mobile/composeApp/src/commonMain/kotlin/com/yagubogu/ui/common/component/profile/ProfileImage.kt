package com.yagubogu.ui.common.component.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yagubogu.ui.theme.Gray300
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_user
import yagubogu.composeapp.generated.resources.profile_image_url_content_description

@Composable
fun ProfileImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model =
            if (LocalInspectionMode.current) {
                Res.drawable.ic_user
            } else {
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            },
        contentDescription = stringResource(Res.string.profile_image_url_content_description),
        modifier =
            modifier
                .border(width = 1.dp, color = Gray300, shape = CircleShape)
                .clip(CircleShape),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileImagePreview() {
    ProfileImage(imageUrl = "")
}
