package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.OAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginParam(
        @Schema(description = "OAuth provider id_token", example = "eyJhbGciOi...")
        String idToken,
        @Schema(description = "OAuth provider", requiredMode = Schema.RequiredMode.REQUIRED, example = "GOOGLE")
        OAuthProvider provider
) {
}
