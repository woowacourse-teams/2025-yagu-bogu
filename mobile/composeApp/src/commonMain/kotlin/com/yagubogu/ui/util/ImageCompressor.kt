package com.yagubogu.ui.util

/**
 * 이미지를 지정한 규격으로 압축한다.
 *
 * 원본 비율을 유지하면서 [spec]의 최대 너비·높이 안에 맞게 축소하고,
 * JPEG로 변환한다. 이미 규격 이하라면 리사이징 없이 품질·용량 제한만 적용한다.
 *
 * @param sourceUri 압축할 원본 이미지의 로컬 URI
 * @param spec 목표 해상도·품질·파일 크기 규격
 * @return 압축된 이미지의 URI, MIME 타입, 파일 크기
 */
expect suspend fun compressImage(
    sourceUri: String,
    spec: ImageCompressionSpec,
): CompressedImage

/**
 * 이미지 압축 규격
 *
 * @property maxWidth 최대 너비 (px)
 * @property maxHeight 최대 높이 (px)
 * @property quality JPEG 품질 (0–100)
 * @property maxFileSizeBytes 최대 파일 크기 (bytes)
 */
data class ImageCompressionSpec(
    val maxWidth: Int,
    val maxHeight: Int,
    val quality: Int,
    val maxFileSizeBytes: Long,
) {
    companion object {
        /** 프로필 이미지: 500×500, JPEG 90%, 5MB */
        val Profile =
            ImageCompressionSpec(
                maxWidth = 500,
                maxHeight = 500,
                quality = 90,
                maxFileSizeBytes = 5L * 1024 * 1024,
            )

        /** 직관 기록 이미지: 1280×1280, JPEG 85%, 5MB */
        val CheckIn =
            ImageCompressionSpec(
                maxWidth = 1280,
                maxHeight = 1280,
                quality = 85,
                maxFileSizeBytes = 5L * 1024 * 1024,
            )
    }
}

/**
 * 압축 완료된 이미지 정보
 *
 * @property uri 압축된 이미지의 로컬 URI
 * @property mimeType MIME 타입 (예: "image/jpeg")
 * @property fileSize 파일 크기 (bytes)
 */
data class CompressedImage(
    val uri: String,
    val fileSize: Long,
    val mimeType: String = MIMETYPE_DEFAULT,
) {
    companion object {
        private const val MIMETYPE_DEFAULT = "image/jpeg"
    }
}
