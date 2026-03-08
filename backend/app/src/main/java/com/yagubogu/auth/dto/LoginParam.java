package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.OAuthProvider;

public record LoginParam(
        String idToken,
        OAuthProvider provider
) {
}
