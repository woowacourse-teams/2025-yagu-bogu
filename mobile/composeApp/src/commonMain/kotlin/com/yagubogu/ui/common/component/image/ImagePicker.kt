package com.yagubogu.ui.common.component.image

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import io.github.ismoy.imagepickerkmp.domain.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.domain.config.CropConfig
import io.github.ismoy.imagepickerkmp.domain.models.CompressionLevel
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.domain.models.MimeType.Companion.ALL_SUPPORTED_TYPES
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.image_selection_failed

@Composable
fun ImagePicker(
    onPhotosSelected: (List<String>) -> Unit,
    onError: (message: StringResource) -> Unit,
    onClosePicker: () -> Unit,
    allowMultiple: Boolean = false,
    selectionLimit: Long = 1,
    enableCrop: Boolean = true,
) {
    val logger: Logger = Logger.withTag("ImagePicker")
    logger.d { "ImagePicker 열림" }
    GalleryPickerLauncher(
        allowMultiple = allowMultiple,
        selectionLimit = selectionLimit,
        mimeTypes = ALL_SUPPORTED_TYPES,
        onPhotosSelected = { photos: List<GalleryPhotoResult> ->
            logger.d { "onPhotosSelected, 사진 개수: ${photos.size}" }
            if (photos.isEmpty()) {
                logger.w { "선택된 사진이 없습니다" }
            } else {
                onPhotosSelected(photos.map { it.uri })
            }
            onClosePicker()
        },
        onError = { exception: Exception ->
            logger.e(exception) { "GalleryPicker 에러 발생" }
            onError(Res.string.image_selection_failed)
            onClosePicker()
        },
        onDismiss = {
            logger.d { "GalleryPicker 닫힘" }
            onClosePicker()
        },
        enableCrop = enableCrop,
        cameraCaptureConfig =
            CameraCaptureConfig(
                compressionLevel = CompressionLevel.HIGH,
                cropConfig =
                    CropConfig(
                        enabled = true,
                        aspectRatioLocked = true,
                        circularCrop = true,
                        squareCrop = false,
                        freeformCrop = false,
                    ),
            ),
    )
}
