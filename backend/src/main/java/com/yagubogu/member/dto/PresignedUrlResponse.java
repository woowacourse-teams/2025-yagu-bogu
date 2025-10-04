package com.yagubogu.member.dto;

public record PresignedUrlResponse(
        String key,
        String url
) {
}
