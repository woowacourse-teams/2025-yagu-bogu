package com.yagubogu.support.refreshtoken;

import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.member.domain.Member;
import com.yagubogu.support.TestFixture;
import java.time.Instant;

public class RefreshTokenBuilder {

    private Member member;
    private Instant expiresAt = TestFixture.getAfter60Minutes();
    private Boolean isRevoked = false;

    public RefreshTokenBuilder member(final Member member) {
        this.member = member;

        return this;
    }

    public RefreshTokenBuilder expiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;

        return this;
    }

    public RefreshTokenBuilder isRevoked(final Boolean isRevoked) {
        this.isRevoked = isRevoked;

        return this;
    }

    public RefreshToken build() {
        RefreshToken refreshToken = RefreshToken.generate(member, expiresAt);
        if (this.isRevoked) {
            refreshToken.revoke();
        }

        return refreshToken;
    }
}
