package com.yagubogu.auth.dto;

public record CreateTokenResponse(
        String accessToken,
        String refreshToken
) {
}
