package com.yagubogu.data.repository.thirdparty

import android.net.Uri

interface ThirdPartyRepository {
    suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>
}
