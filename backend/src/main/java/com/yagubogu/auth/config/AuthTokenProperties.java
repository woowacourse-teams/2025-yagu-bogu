package com.yagubogu.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "security.token")
public class AuthTokenProperties {

    private TokenProperties accessToken;
    private TokenProperties refreshToken;

    @Setter
    @Getter
    public static class TokenProperties {
        private String secretKey;
        private long expiresIn;
    }
}
