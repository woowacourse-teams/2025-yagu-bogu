package com.yagubogu.auth.client;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import java.time.Instant;

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
                "name",
                "picture",
                "givenName",
                "familyName",
                "ko"
        );
    }
}
