package com.yagubogu.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
