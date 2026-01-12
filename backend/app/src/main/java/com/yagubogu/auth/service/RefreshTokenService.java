package com.yagubogu.auth.service;

import com.yagubogu.auth.config.AuthTokenProperties;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.member.domain.Member;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenProperties authTokenProperties;

    @Transactional
    public String issue(final Member member) {
        Instant expiresAt = calculateExpireAt();
        RefreshToken refreshToken = RefreshToken.generate(member, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getId();
    }

    private Instant calculateExpireAt() {
        long expiresIn = authTokenProperties.getRefreshToken().getExpiresIn();

        return Instant.now().plus(expiresIn, ChronoUnit.SECONDS);
    }
}
