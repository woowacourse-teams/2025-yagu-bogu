package com.yagubogu.talk.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import com.yagubogu.global.exception.RateLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InMemoryRateLimiterTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Test
    @DisplayName("초당 3개까지 허용")
    void allowThreeRequestsPerSecond() {
        // given
        String key = "test:user:1";

        // when & then
        assertThatNoException().isThrownBy(() -> {
            rateLimiter.checkLimit(key, 3, 1);
            rateLimiter.checkLimit(key, 3, 1);
            rateLimiter.checkLimit(key, 3, 1);
        });
    }

    @Test
    @DisplayName("초당 4개 요청 시 예외 발생")
    void throwExceptionOnFourthRequest() {
        // given
        String key = "test:user:2";

        // when & then
        rateLimiter.checkLimit(key, 3, 1);
        rateLimiter.checkLimit(key, 3, 1);
        rateLimiter.checkLimit(key, 3, 1);

        assertThatThrownBy(() -> rateLimiter.checkLimit(key, 3, 1))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("1초 후에는 다시 요청 가능")
    void resetAfterWindow() throws InterruptedException {
        // given
        String key = "test:user:3";

        // when
        rateLimiter.checkLimit(key, 3, 1);
        rateLimiter.checkLimit(key, 3, 1);
        rateLimiter.checkLimit(key, 3, 1);

        Thread.sleep(1100); // 1.1초 대기

        // then
        assertThatNoException().isThrownBy(() ->
                rateLimiter.checkLimit(key, 3, 1)
        );
    }
}
