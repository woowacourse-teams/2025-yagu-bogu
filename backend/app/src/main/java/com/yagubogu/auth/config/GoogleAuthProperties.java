package com.yagubogu.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleAuthProperties(
        String baseUri,
        String tokenInfoUri,
        String clientId,
        Duration readTimeout,
        Duration connectTimeout
) {
}
