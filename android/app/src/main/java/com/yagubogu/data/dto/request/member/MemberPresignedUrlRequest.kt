package com.yagubogu.data.dto.request.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberPresignedUrlRequest(
    @SerialName("contentType")
    val contentType: String,      // 업로드할 파일의 MIME 타입
    @SerialName("contentLength")
    val contentLength: Long        // 업로드할 파일 크기(바이트)
)