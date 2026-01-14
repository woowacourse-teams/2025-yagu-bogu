package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.GoogleAuthParam;
import com.yagubogu.auth.dto.LoginParam;
import java.time.Instant;
import java.util.UUID;

public class FakeAuthGateway implements AuthGateway {

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        return new GoogleAuthParam(
                "accounts.google.com",
                "sub-test-unique-01",
                "azp",
                "this-is-client-id",
                111L, Instant.now().plusSeconds(3000).getEpochSecond(),
                "email",
                true,
                UUID.randomUUID().toString().substring(0, 10),
                "picture",
                "givenName",
                "familyName",
                "ko"
        );
    }
}
