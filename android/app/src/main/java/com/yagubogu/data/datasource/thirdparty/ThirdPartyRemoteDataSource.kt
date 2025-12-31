package com.yagubogu.data.datasource.thirdparty

import android.content.ContentResolver
import android.net.Uri
import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.util.safeKtorApiCall
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import java.io.InputStream
import javax.inject.Inject

class ThirdPartyRemoteDataSource @Inject constructor(
    private val thirdPartyApiService: ThirdPartyApiService,
    private val contentResolver: ContentResolver,
) : ThirdPartyDataSource {
    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> =
        safeKtorApiCall<Unit> {
            val requestBody: OutgoingContent =
                createRequestBody(imageFileUri, contentType, contentLength)
            thirdPartyApiService.putImageToS3(url, requestBody)
        }

    private fun createRequestBody(
        uri: Uri,
        contentType: String,
        contentLength: Long,
    ): OutgoingContent =
        object : OutgoingContent.ReadChannelContent() {
            override val contentType: ContentType = ContentType.parse(contentType)

            override val contentLength: Long = contentLength

            override fun readFrom(): ByteReadChannel {
                val inputStream: InputStream =
                    contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Failed to open input stream for $uri")
                return inputStream.toByteReadChannel()
            }
        }
}
