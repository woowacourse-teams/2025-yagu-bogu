package com.yagubogu.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleAuthProperties(
        String baseUri,
        String tokenInfoUri,
        String clientId
) {
}
