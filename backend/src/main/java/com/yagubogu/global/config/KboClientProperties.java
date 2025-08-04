package com.yagubogu.global.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.kbo")
public record KboClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        String contentType
) {
}
