package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AppleAuthParam;
import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.GoogleAuthParam;
import com.yagubogu.auth.dto.LoginParam;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FakeAuthGateway implements AuthGateway {

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        if (loginParam.provider() != null && loginParam.provider().isApple()) {
            return new AppleAuthParam(
                    "https://appleid.apple.com",
                    "apple-sub-test-01",
                    List.of("com.example.app"),
                    111L,
                    Instant.now().plusSeconds(3000).getEpochSecond(),
                    "apple@example.com",
                    true,
                    false
            );
        }

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
