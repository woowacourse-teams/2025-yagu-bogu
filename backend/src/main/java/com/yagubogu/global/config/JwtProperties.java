package com.yagubogu.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private TokenProperties accessToken;
    private TokenProperties refreshToken;

    @Setter
    @Getter
    public static class TokenProperties {
        private String secretKey;
        private long expireLength;
    }
}
