package com.yagubogu.auth.dto.v1;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
