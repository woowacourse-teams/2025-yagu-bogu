package com.yagubogu.member.dto;

public record PreSignedUrlRequest(
        String contentType,
        long contentLength
) {
}
