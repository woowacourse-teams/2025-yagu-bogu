package com.yagubogu.data.repository.thirdparty

interface ThirdPartyRepository {
    suspend fun uploadImageToS3(
        url: String,
        imageFileUri: String,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>
}
