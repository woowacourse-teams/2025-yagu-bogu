package com.yagubogu.reward.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.gift")
public record KakaoGiftProperties(
        String baseUrl,
        String apiKey,
        String templateToken,
        Duration connectTimeout,
        Duration readTimeout
) {
}
