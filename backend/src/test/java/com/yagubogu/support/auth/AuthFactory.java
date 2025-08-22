package com.yagubogu.support.auth;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.member.domain.Role;

public class AuthFactory {

    private static final String BEARER = "Bearer ";

    private final AuthTokenProvider authTokenProvider;

    public AuthFactory(
            final AuthTokenProvider authTokenProvider
    ) {
        this.authTokenProvider = authTokenProvider;
    }

    public String getAccessTokenByMemberId(long memberId, Role role) {
        MemberClaims claims = new MemberClaims(memberId, role);
        String jwt = authTokenProvider.createAccessToken(claims);

        return BEARER + jwt;
    }
}
