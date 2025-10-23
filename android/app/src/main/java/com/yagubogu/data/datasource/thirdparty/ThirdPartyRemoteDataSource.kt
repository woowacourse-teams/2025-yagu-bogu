package com.yagubogu.data.datasource.thirdparty

import android.content.ContentResolver
import android.net.Uri
import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.util.safeApiCall
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.InputStream
import javax.inject.Inject

class ThirdPartyRemoteDataSource
    @Inject
    constructor(
        private val thirdPartyApiService: ThirdPartyApiService,
        private val contentResolver: ContentResolver,
    ) : ThirdPartyDataSource {
        override suspend fun uploadImageToS3(
            url: String,
            imageFileUri: Uri,
            contentType: String,
            contentLength: Long,
        ): Result<Unit> =
            safeApiCall {
                val requestBody: RequestBody =
                    createRequestBody(imageFileUri, contentType, contentLength)
                thirdPartyApiService.putImageToS3(url, requestBody)
            }

        private fun createRequestBody(
            uri: Uri,
            contentType: String,
            contentLength: Long,
        ): RequestBody =
            object : RequestBody() {
                override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

                override fun contentLength(): Long = contentLength

                override fun writeTo(sink: BufferedSink) {
                    contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                        sink.writeAll(inputStream.source())
                    }
                }
            }
    }
