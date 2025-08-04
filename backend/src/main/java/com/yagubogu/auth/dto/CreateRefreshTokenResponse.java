package com.yagubogu.auth.dto;

public record CreateRefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
