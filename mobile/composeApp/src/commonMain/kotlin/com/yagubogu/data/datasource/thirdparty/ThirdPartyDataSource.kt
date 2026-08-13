package com.yagubogu.data.datasource.thirdparty

interface ThirdPartyDataSource {
    suspend fun uploadImageToS3(
        url: String,
        imageFileUri: String,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>
}
