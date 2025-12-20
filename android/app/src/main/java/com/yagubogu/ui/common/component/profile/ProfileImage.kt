package com.yagubogu.ui.common.component.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray300

@Composable
fun ProfileImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model =
            if (LocalInspectionMode.current) {
                R.drawable.ic_user
            } else {
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            },
        contentDescription = stringResource(R.string.profile_image_url_content_description),
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
