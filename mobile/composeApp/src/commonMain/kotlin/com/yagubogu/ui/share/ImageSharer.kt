package com.yagubogu.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

interface ImageSharer {
    fun shareImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
    )
}

@Composable
expect fun rememberImageSharer(): ImageSharer

expect fun ImageBitmap.toByteArray(): ByteArray
