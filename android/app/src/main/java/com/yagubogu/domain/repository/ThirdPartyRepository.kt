package com.yagubogu.domain.repository

import android.net.Uri

interface ThirdPartyRepository {
    suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>
}
