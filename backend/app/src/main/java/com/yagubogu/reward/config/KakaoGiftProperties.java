package com.yagubogu.reward.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kakao.gift")
public record KakaoGiftProperties(
        String baseUrl,
        @NotBlank(message = "Kakao Gift API key must be configured") String apiKey,
        @NotBlank(message = "Kakao Gift template token must be configured") String templateToken,
        Duration connectTimeout,
        Duration readTimeout
) {
}
