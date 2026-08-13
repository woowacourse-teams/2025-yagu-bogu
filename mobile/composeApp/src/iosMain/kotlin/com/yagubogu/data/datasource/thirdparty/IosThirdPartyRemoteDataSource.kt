package com.yagubogu.data.datasource.thirdparty

import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.util.safeApiCall
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class IosThirdPartyRemoteDataSource(
    private val thirdPartyApiService: ThirdPartyApiService,
) : ThirdPartyDataSource {
    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: String,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> =
        safeApiCall {
            // 로컬 파일 경로용 URL 객체 생성
            val nsUrl =
                when {
                    imageFileUri.startsWith("file://") -> NSURL(string = imageFileUri)
                    else -> NSURL.fileURLWithPath(imageFileUri)
                }

            val nsData =
                NSData.dataWithContentsOfURL(nsUrl)
                    ?: throw IllegalStateException("Failed to read data from $imageFileUri (Resolved URL: ${nsUrl.absoluteString})")

            // 데이터 크기 검증 (넘겨받은 contentLength와 실제 읽은 데이터 크기 비교)
            val actualLength = nsData.length.toLong()

            val byteArray = ByteArray(nsData.length.toInt())
            if (byteArray.isNotEmpty()) {
                byteArray.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
            }

            val requestBody =
                object : OutgoingContent.ByteArrayContent() {
                    override val contentType: ContentType = ContentType.parse(contentType)
                    override val contentLength: Long = actualLength

                    override fun bytes(): ByteArray = byteArray
                }

            thirdPartyApiService.putImageToS3(url, requestBody)
        }
}
