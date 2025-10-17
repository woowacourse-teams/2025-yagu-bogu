package com.yagubogu.member.dto;

public record PresignedUrlStartResponse(
        String key,
        String url
) {
}
