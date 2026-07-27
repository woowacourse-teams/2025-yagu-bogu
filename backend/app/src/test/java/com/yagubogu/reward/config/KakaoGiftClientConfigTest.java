package com.yagubogu.reward.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoGiftClientConfigTest {

    private final KakaoGiftClientConfig config = new KakaoGiftClientConfig();

    @DisplayName("0ms 타임아웃을 허용한다")
    @Test
    void allowZeroTimeout() {
        KakaoGiftProperties properties = properties(Duration.ZERO, Duration.ZERO);

        assertThatCode(() -> config.kakaoGiftRequestFactory(properties))
                .doesNotThrowAnyException();
    }

    @DisplayName("음수 타임아웃을 거부한다")
    @Test
    void rejectNegativeTimeout() {
        KakaoGiftProperties properties = properties(Duration.ofMillis(-1), Duration.ofSeconds(1));

        assertThatThrownBy(() -> config.kakaoGiftRequestFactory(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kakao Gift timeout must be between 0ms and Integer.MAX_VALUE ms");
    }

    @DisplayName("int 범위를 초과하는 타임아웃을 거부한다")
    @Test
    void rejectOverflowingTimeout() {
        Duration overflowingTimeout = Duration.ofMillis((long) Integer.MAX_VALUE + 1);
        KakaoGiftProperties properties = properties(Duration.ofSeconds(1), overflowingTimeout);

        assertThatThrownBy(() -> config.kakaoGiftRequestFactory(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kakao Gift timeout must be between 0ms and Integer.MAX_VALUE ms");
    }

    private KakaoGiftProperties properties(
            final Duration connectTimeout,
            final Duration readTimeout
    ) {
        return new KakaoGiftProperties(
                "https://gift.example.com",
                "api-key",
                "template-token",
                connectTimeout,
                readTimeout
        );
    }
}
