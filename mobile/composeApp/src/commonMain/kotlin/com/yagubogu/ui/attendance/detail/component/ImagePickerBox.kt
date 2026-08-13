package com.yagubogu.ui.attendance.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_delete_photo
import yagubogu.composeapp.generated.resources.attendance_detail_image_picker
import yagubogu.composeapp.generated.resources.ic_camera
import yagubogu.composeapp.generated.resources.ic_close

@Composable
fun ImagePickerBoxRow(
    onAddClick: () -> Unit,
    onDeleteClick: (index: Int) -> Unit,
    images: ImmutableList<String?>,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        images.forEachIndexed { index, image ->
            ImagePickerBox(
                image = image,
                modifier = Modifier.weight(1f),
                onAddClick = onAddClick,
                onDeleteClick = { onDeleteClick(index) },
            )
        }
    }
}

@Composable
fun ImagePickerBox(
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: String? = null,
) {
    if (image == null) {
        EmptyImageBox(
            onClick = onAddClick,
            modifier = modifier,
        )
    } else {
        ImageBox(
            image = image,
            onDeleteClick = onDeleteClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun EmptyImageBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .border(1.dp, Gray400, RoundedCornerShape(12.dp))
                .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painterResource(Res.drawable.ic_camera),
                contentDescription = null,
                tint = Gray400,
            )
            Text(
                text = stringResource(Res.string.attendance_detail_image_picker),
                style = PretendardRegular12,
                color = Gray400,
            )
        }
    }
}

@Composable
private fun ImageBox(
    image: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .border(1.dp, Gray400, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
        )
        DeleteIcon(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .noRippleClickable { onDeleteClick() },
        )
    }
}

@Composable
private fun DeleteIcon(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(Gray400, CircleShape).size(20.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_close),
            contentDescription = stringResource(Res.string.attendance_detail_delete_photo),
            tint = White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun EmptyImageBoxPreview() {
    Surface(modifier = Modifier.size(200.dp).padding(20.dp)) {
        ImagePickerBox(
            image = null,
            onAddClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun ImageBoxPreview() {
    Surface(modifier = Modifier.size(200.dp).padding(20.dp)) {
        ImagePickerBox(
            image = "",
            onAddClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun ImagePickerBoxRowPreview() {
    Surface(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        ImagePickerBoxRow(
            images = persistentListOf("", "", null),
            onAddClick = {},
            onDeleteClick = {},
        )
    }
}
