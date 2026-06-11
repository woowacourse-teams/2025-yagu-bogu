package com.yagubogu.ui.share

import yagubogu.composeapp.generated.resources.Res

private const val SHARE_IMAGE_RESOURCE_PATH = "files/attendance_share_example.png"
private const val SHARE_IMAGE_FILE_NAME = "attendance_share_example.png"
private const val SHARE_IMAGE_MIME_TYPE = "image/png"

suspend fun shareAttendanceExampleImage(imageSharer: ImageSharer) {
    val imageBytes = Res.readBytes(SHARE_IMAGE_RESOURCE_PATH)
    imageSharer.shareImage(
        imageBytes = imageBytes,
        fileName = SHARE_IMAGE_FILE_NAME,
        mimeType = SHARE_IMAGE_MIME_TYPE,
    )
}
