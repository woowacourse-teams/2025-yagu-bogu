package com.yagubogu.place.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 서비스키 누락이 기동 시점에 잡히는지 검증한다.
 * GitHub Actions는 등록되지 않은 시크릿을 빈 문자열로 치환하므로, 검증이 없으면 앱이 정상 기동한 뒤
 * 매일 새벽 3시 폴링에서만 전부 실패한다 — 실제로 그 경로로 장애가 하루 동안 드러나지 않았다.
 */
class TourApiPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TourApiPropertiesConfig.class)
            .withPropertyValues(
                    "tour.api.base-url=https://apis.data.go.kr/B551011/KorService2",
                    "tour.api.radius=3000",
                    "tour.api.num-of-rows=50",
                    "tour.api.request-interval=100ms",
                    "tour.api.connect-timeout=10s",
                    "tour.api.read-timeout=30s"
            );

    @DisplayName("서비스키가 비어 있으면 기동에 실패한다")
    @Test
    void 서비스키가_비어_있으면_기동_실패() {
        contextRunner.withPropertyValues("tour.api.service-key=")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("TOUR_API_SERVICE_KEY"));
    }

    @DisplayName("서비스키가 있으면 정상 바인딩된다")
    @Test
    void 서비스키가_있으면_정상_바인딩() {
        contextRunner.withPropertyValues("tour.api.service-key=decoded-service-key")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(TourApiProperties.class).getServiceKey())
                            .isEqualTo("decoded-service-key");
                });
    }

    @Configuration
    @EnableConfigurationProperties(TourApiProperties.class)
    static class TourApiPropertiesConfig {
    }
}
