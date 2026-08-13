package com.yagubogu.data.datasource.thirdparty

import android.content.ContentResolver
import androidx.core.net.toUri
import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.util.safeApiCall
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

class AndroidThirdPartyRemoteDataSource(
    private val thirdPartyApiService: ThirdPartyApiService,
    private val contentResolver: ContentResolver,
) : ThirdPartyDataSource {
    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: String,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> =
        safeApiCall {
            val requestBody: OutgoingContent =
                createRequestBody(imageFileUri, contentType, contentLength)
            thirdPartyApiService.putImageToS3(url, requestBody)
        }

    private fun createRequestBody(
        uri: String,
        contentType: String,
        contentLength: Long,
    ): OutgoingContent =
        object : OutgoingContent.ReadChannelContent() {
            override val contentType: ContentType = ContentType.parse(contentType)
            override val contentLength: Long = contentLength

            override fun readFrom(): ByteReadChannel {
                // .use를 제거하고 직접 반환, Ktor 엔진이 데이터를 다 보낸 후 내부적으로 채널을 닫음.
                val inputStream =
                    contentResolver.openInputStream(uri.toUri())
                        ?: throw IllegalStateException("Failed to open input stream for $uri")

                return inputStream.toByteReadChannel()
            }
        }
}
