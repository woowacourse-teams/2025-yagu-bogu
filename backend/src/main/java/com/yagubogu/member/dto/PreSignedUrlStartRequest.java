package com.yagubogu.member.dto;

public record PreSignedUrlStartRequest(
        String contentType,
        long contentLength
) {
}
