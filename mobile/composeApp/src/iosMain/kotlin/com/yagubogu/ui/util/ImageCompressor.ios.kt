package com.yagubogu.ui.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

/**
 * UIKit을 사용해 이미지를 압축한다.
 * [scaledDimensions]로 원본 비율을 유지한 target 크기를 계산하고,
 * 리사이징 후 JPEG로 변환해 임시 디렉터리에 저장한다.
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun compressImage(
    sourceUri: String,
    spec: ImageCompressionSpec,
): CompressedImage {
    // 1. 이미지 로드
    // file:// URI와 순수 경로 모두 처리: NSURL.path는 percent-decoding도 수행함
    val path =
        NSURL(string = sourceUri).path
            ?: NSURL.fileURLWithPath(sourceUri).path
            ?: error("경로를 가져올 수 없음: $sourceUri")
    val originalImage = UIImage.imageWithContentsOfFile(path) ?: error("이미지를 불러올 수 없음: $path")

    // 2. 리사이징 (원본 비율 유지, spec 최대 크기 이내)
    val (targetWidth, targetHeight) =
        scaledDimensions(
            originalWidth = originalImage.size.useContents { width },
            originalHeight = originalImage.size.useContents { height },
            maxWidth = spec.maxWidth.toDouble(),
            maxHeight = spec.maxHeight.toDouble(),
        )

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
    originalImage.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    if (resizedImage == null) error("이미지 리사이징 실패")

    // 3. JPEG 압축
    val quality = spec.quality / 100.0
    val imageData = UIImageJPEGRepresentation(resizedImage, quality) ?: error("이미지 압축 실패")

    // 4. 임시 파일로 저장 (파일 크기 및 URI 획득을 위함)
    val timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong()
    val tmpDir = NSTemporaryDirectory()
    val tempFilePath =
        if (tmpDir.endsWith("/")) "${tmpDir}image_$timestamp.jpg" else "$tmpDir/image_$timestamp.jpg"

    if (!imageData.writeToFile(tempFilePath, true)) error("임시 파일 저장 실패")

    return CompressedImage(
        uri = tempFilePath,
        fileSize = imageData.length.toLong(),
    )
}

/**
 * 원본 비율을 유지하면서 [maxWidth]×[maxHeight] 안에 맞는 크기를 반환한다.
 * 이미 범위 이하면 원본 크기를 그대로 반환한다.
 */
private fun scaledDimensions(
    originalWidth: Double,
    originalHeight: Double,
    maxWidth: Double,
    maxHeight: Double,
): Pair<Double, Double> {
    if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
        return originalWidth to originalHeight
    }
    val scale = minOf(maxWidth / originalWidth, maxHeight / originalHeight)
    return originalWidth * scale to originalHeight * scale
}
