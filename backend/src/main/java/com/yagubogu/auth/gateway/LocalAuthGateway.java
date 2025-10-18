package com.yagubogu.auth.gateway;

import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.GoogleAuthParam;
import com.yagubogu.auth.dto.LoginParam;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("local")
@Component
public class LocalAuthGateway implements AuthGateway {

    private final GoogleAuthProperties googleAuthProperties;

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        return new GoogleAuthParam(
                "accounts.google.com",
                "local-sub-id",
                "azp",
                googleAuthProperties.clientId(),
                111L,
                9999999999L,
                "local@example.com",
                true,
                "초코송이쿠키사탕별에서춤추고노래하는행복개발자밍트",
                "https://example.com/profile.png",
                "이름",
                "성",
                "ko"
        );
    }
}
