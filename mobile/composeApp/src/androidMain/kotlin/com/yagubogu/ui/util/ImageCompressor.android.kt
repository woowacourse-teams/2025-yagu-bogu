package com.yagubogu.ui.util

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import org.koin.core.context.GlobalContext
import java.io.File

/**
 * Zelory Compressor 라이브러리를 사용해 이미지를 압축한다.
 * [scaledDimensions]로 원본 비율을 유지한 target 크기를 계산한 뒤 적용한다.
 */
actual suspend fun compressImage(
    sourceUri: String,
    spec: ImageCompressionSpec,
): CompressedImage {
    val context = GlobalContext.get().get<Application>()
    val originalFile = File(sourceUri.toUri().path ?: error("경로를 찾을 수 없음: $sourceUri"))

    val (targetWidth, targetHeight) = scaledDimensions(originalFile, spec.maxWidth, spec.maxHeight)

    val compressedFile =
        Compressor.compress(context, originalFile) {
            resolution(targetWidth, targetHeight)
            quality(spec.quality)
            format(Bitmap.CompressFormat.JPEG)
            size(spec.maxFileSizeBytes)
        }

    return CompressedImage(
        uri = Uri.fromFile(compressedFile).toString(),
        fileSize = compressedFile.length(),
    )
}

/**
 * 원본 비율을 유지하면서 [maxWidth]×[maxHeight] 안에 맞는 크기를 반환한다.
 * 이미 범위 이하면 원본 크기를 그대로 반환한다.
 */
private fun scaledDimensions(
    file: File,
    maxWidth: Int,
    maxHeight: Int,
): Pair<Int, Int> {
    val options =
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, this)
        }
    val originalWidth = options.outWidth
    val originalHeight = options.outHeight

    if (originalWidth <= 0 || originalHeight <= 0) error("이미지 디코딩 실패: ${file.absolutePath}")

    if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
        return originalWidth to originalHeight
    }
    val scale = minOf(maxWidth.toFloat() / originalWidth, maxHeight.toFloat() / originalHeight)
    return (originalWidth * scale).toInt() to (originalHeight * scale).toInt()
}
