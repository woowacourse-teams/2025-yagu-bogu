package com.yagubogu.member.dto.v1;

public record PreSignedUrlStartRequest(
        String contentType,
        long contentLength
) {
}
