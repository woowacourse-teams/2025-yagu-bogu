package com.yagubogu.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.apple")
public record AppleAuthProperties(
        String issuer,
        String clientId,
        String jwksUri
) {
}
