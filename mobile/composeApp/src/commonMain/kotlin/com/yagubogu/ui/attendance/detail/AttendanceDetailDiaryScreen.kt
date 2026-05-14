package com.yagubogu.ui.attendance.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.attendance.detail.component.ImagePickerBoxRow
import com.yagubogu.ui.attendance.detail.component.ImageSlider
import com.yagubogu.ui.attendance.detail.component.ImageViewerDialog
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.DiaryImageItem
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import com.yagubogu.ui.common.component.image.ImagePicker
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold16
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.noRippleClickable
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_diary_edit
import yagubogu.composeapp.generated.resources.attendance_detail_diary_placeholder
import yagubogu.composeapp.generated.resources.attendance_detail_diary_save
import yagubogu.composeapp.generated.resources.attendance_detail_tab_diary
import yagubogu.composeapp.generated.resources.ic_pencil

@Composable
fun AttendanceDetailDiaryScreen(
    uiState: AttendanceDetailDiaryUiState,
    onImagesSelected: (images: List<String>) -> Unit,
    onImageDeleted: (index: Int) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: (comment: String) -> Unit,
    onImagePickerError: (message: StringResource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
    ) {
        AttendanceDetailDiaryTitle(
            showEditButton = uiState.mode == DiaryMode.READ,
            onEditClick = onEditClick,
        )
        Spacer(modifier = Modifier.height(10.dp))

        when (uiState.mode) {
            DiaryMode.READ -> ReadingDiaryPage(uiState = uiState)

            DiaryMode.WRITE ->
                WritingDiaryPage(
                    uiState = uiState,
                    onImagesSelected = onImagesSelected,
                    onImageDeleted = onImageDeleted,
                    onSaveClick = onSaveClick,
                    onImagePickerError = onImagePickerError,
                )
        }
    }
}

@Composable
private fun AttendanceDetailDiaryTitle(
    showEditButton: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(Res.drawable.ic_pencil),
            contentDescription = stringResource(Res.string.attendance_detail_tab_diary),
            tint = Primary500,
        )
        Text(
            text = stringResource(Res.string.attendance_detail_tab_diary),
            style = PretendardBold20,
        )

        if (showEditButton) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.attendance_detail_diary_edit),
                style = PretendardRegular12,
                color = Gray500,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.noRippleClickable { onEditClick() },
            )
        }
    }
}

@Composable
private fun WritingDiaryPage(
    uiState: AttendanceDetailDiaryUiState,
    onImagesSelected: (images: List<String>) -> Unit,
    onImageDeleted: (index: Int) -> Unit,
    onSaveClick: (comment: String) -> Unit,
    onImagePickerError: (message: StringResource) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isGalleryOpen by remember { mutableStateOf(false) }
    var comment by remember(uiState.comment) { mutableStateOf(uiState.comment) }
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ImagePickerBoxRow(
            images = uiState.imageSlots,
            onAddClick = { isGalleryOpen = true },
            onDeleteClick = onImageDeleted,
        )
        DiaryTextField(
            readOnly = false,
            value = comment,
            onValueChange = {
                if (it.length <= AttendanceDetailViewModel.DIARY_MAX_LENGTH) comment = it
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        if (!isKeyboardVisible) {
            DiarySaveButton(onClick = { onSaveClick(comment) })
        }
    }

    if (isGalleryOpen) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Gray900.copy(alpha = 0.3f))
                    .systemBarsPadding()
                    .pointerInput(Unit) { detectTapGestures { } }, // 배경 터치 차단
        ) {
            ImagePicker(
                allowMultiple = true,
                selectionLimit = uiState.emptyImageCount,
                enableCrop = false,
                onPhotosSelected = { uris: List<String> ->
                    isGalleryOpen = false
                    onImagesSelected(uris)
                },
                onError = onImagePickerError,
                onClosePicker = { isGalleryOpen = false },
            )
        }
    }
}

@Composable
private fun ReadingDiaryPage(
    uiState: AttendanceDetailDiaryUiState,
    modifier: Modifier = Modifier,
) {
    var viewerInitialPage by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (!uiState.isImageEmpty) {
            ImageSlider(
                images = uiState.imageUris,
                onImageClick = { index -> viewerInitialPage = index },
            )
        }
        DiaryTextField(
            readOnly = true,
            value = uiState.comment,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }

    viewerInitialPage?.let { page ->
        ImageViewerDialog(
            images = uiState.imageUris,
            initialPage = page,
            onDismiss = { viewerInitialPage = null },
        )
    }
}

@Composable
private fun DiaryTextField(
    readOnly: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        textStyle = PretendardRegular.copy(fontSize = 14.sp),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.attendance_detail_diary_placeholder),
                        style = PretendardRegular.copy(fontSize = 14.sp, color = Gray400),
                    )
                }
                innerTextField()
                if (!readOnly) {
                    Text(
                        text = "${value.length}/${AttendanceDetailViewModel.DIARY_MAX_LENGTH}",
                        style = PretendardRegular12.copy(color = Gray400),
                        modifier = Modifier.align(Alignment.BottomEnd),
                    )
                }
            }
        },
    )
}

@Composable
private fun DiarySaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = White,
            ),
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.attendance_detail_diary_save),
            style = PretendardBold16,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceDetailDiaryScreenWritingPagePreview() {
    AttendanceDetailDiaryScreen(
        uiState =
            AttendanceDetailDiaryUiState(
                images = persistentListOf(DiaryImageItem(), DiaryImageItem(), DiaryImageItem()),
            ),
        onImagesSelected = {},
        onImageDeleted = {},
        onEditClick = {},
        onSaveClick = { _ -> },
        onImagePickerError = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AttendanceDetailDiaryScreenReadingPagePreview() {
    AttendanceDetailDiaryScreen(
        uiState =
            AttendanceDetailDiaryUiState(
                mode = DiaryMode.READ,
                images = persistentListOf(DiaryImageItem(), DiaryImageItem(), DiaryImageItem()),
                comment =
                    """
                    어쩌구저쩌구 그런데 직관 기록 최대 몇 자로 해야되지? 제한이 있어야 할 거 같긴 한데.. 몇자로 제한함? 백엔드에서 정했겠지? 크림이 알아서 하겠지? 백엔드에서 정했으면 물어봐야 됨. 어쩌구 저쩌구 직관 기록..

                    와! 오늘 이겼다 어쩌구.. 오늘 졌다 어쩌구 이런 걸 작성하는 건가
                    """.trimIndent(),
            ),
        onImagesSelected = {},
        onImageDeleted = {},
        onEditClick = {},
        onSaveClick = { _ -> },
        onImagePickerError = {},
    )
}
