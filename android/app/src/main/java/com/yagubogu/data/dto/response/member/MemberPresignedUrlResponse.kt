package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MemberPresignedUrlResponse(
    @SerialName("key")
    val key: String,               // S3 객체 키(클라이언트가 이 키로 업로드)
    @SerialName("url")
    val url: String                // PUT 요청용 Pre-Signed URL(유효기간 내 1회 업로드)
)