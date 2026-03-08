package com.yagubogu.auth.gateway;

import com.yagubogu.auth.config.AppleAuthProperties;
import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.dto.AppleAuthParam;
import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.GoogleAuthParam;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.member.domain.OAuthProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("local")
@Component
public class LocalAuthGateway implements AuthGateway {

    private final GoogleAuthProperties googleAuthProperties;
    private final AppleAuthProperties appleAuthProperties;

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        OAuthProvider provider = loginParam.provider();
        if (provider != null && provider.isApple()) {
            return new AppleAuthParam(
                    appleAuthProperties.issuer(),
                    "local-apple-sub-id",
                    List.of(appleAuthProperties.clientId()),
                    111L,
                    9999999999L,
                    "local-apple@example.com",
                    true,
                    false
            );
        }

        return new GoogleAuthParam(
                "accounts.google.com",
                "local-sub-id",
                "azp",
                googleAuthProperties.clientId(),
                111L,
                9999999999L,
                "local@example.com",
                true,
                "local-user",
                "https://example.com/profile.png",
                "givenName",
                "familyName",
                "ko"
        );
    }
}
