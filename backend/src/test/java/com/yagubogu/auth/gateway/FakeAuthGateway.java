package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import java.time.Instant;
import java.util.UUID;

public class FakeAuthGateway implements AuthGateway {

    @Override
    public AuthResponse validateToken(final LoginRequest loginRequest) {
        return new GoogleAuthResponse(
                "accounts.google.com",
                "sub-test-unique-01",
                "azp",
                "this-is-client-id",
                111L, Instant.now().plusSeconds(3000).getEpochSecond(),
                "email",
                true,
                UUID.randomUUID().toString(),
                "picture",
                "givenName",
                "familyName",
                "ko"
        );
    }
}
