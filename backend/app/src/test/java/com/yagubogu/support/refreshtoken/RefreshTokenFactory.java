package com.yagubogu.support.refreshtoken;

import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import java.util.function.Consumer;

public class RefreshTokenFactory {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenFactory(final RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken save(final Consumer<RefreshTokenBuilder> customizer) {
        RefreshTokenBuilder builder = new RefreshTokenBuilder();
        customizer.accept(builder);
        RefreshToken member = builder.build();

        return refreshTokenRepository.save(member);
    }
}

