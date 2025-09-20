package com.yagubogu.auth.gateway;

import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("local")
@Component
public class LocalAuthGateway implements AuthGateway {

    private final GoogleAuthProperties googleAuthProperties;

    @Override
    public AuthResponse validateToken(final LoginRequest loginRequest) {
        return new GoogleAuthResponse(
                "accounts.google.com",
                "local-sub-id",
                "azp",
                googleAuthProperties.clientId(),
                111L,
                9999999999L,
                "local@example.com",
                true,
                UUID.randomUUID().toString(),
                "https://example.com/profile.png",
                "이름",
                "성",
                "ko"
        );
    }
}
