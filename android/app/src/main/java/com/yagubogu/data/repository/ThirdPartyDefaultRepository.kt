package com.yagubogu.data.repository

import android.net.Uri
import com.yagubogu.data.datasource.ThirdPartyDataSource
import com.yagubogu.domain.repository.ThirdPartyRepository

class ThirdPartyDefaultRepository(
    private val thirdPartyDataSource: ThirdPartyDataSource,
) : ThirdPartyRepository {
    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> = thirdPartyDataSource.uploadImageToS3(url, imageFileUri, contentType, contentLength)
}
