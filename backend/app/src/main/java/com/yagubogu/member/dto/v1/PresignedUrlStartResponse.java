package com.yagubogu.member.dto.v1;

public record PresignedUrlStartResponse(
        String key,
        String url
) {
}
