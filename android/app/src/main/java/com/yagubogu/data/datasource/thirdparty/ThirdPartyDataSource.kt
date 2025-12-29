package com.yagubogu.data.datasource.thirdparty

import android.net.Uri

interface ThirdPartyDataSource {
    suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>
}
