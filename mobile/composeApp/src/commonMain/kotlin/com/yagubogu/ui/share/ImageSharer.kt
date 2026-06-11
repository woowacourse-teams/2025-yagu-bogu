package com.yagubogu.ui.share

import androidx.compose.runtime.Composable

interface ImageSharer {
    fun shareImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
    )
}

@Composable
expect fun rememberImageSharer(): ImageSharer
