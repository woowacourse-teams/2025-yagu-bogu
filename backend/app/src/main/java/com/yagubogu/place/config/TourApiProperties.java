package com.yagubogu.place.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 서비스키는 기동 시점에 검증한다.
 * GitHub Actions는 등록되지 않은 시크릿을 경고 없이 빈 문자열로 치환하기 때문에,
 * 검증이 없으면 앱이 정상 기동하고 헬스체크까지 통과한 뒤 매일 새벽 3시 폴링에서만 전부 실패한다.
 * (하루 한 번 도는 작업이라 발견까지 24시간이 걸린다.)
 */
@Getter
@Validated
@ConfigurationProperties(prefix = "tour.api")
public class TourApiProperties {

    @NotBlank(message = "TOUR_API_SERVICE_KEY가 비어 있습니다. 공공데이터포털에서 발급받은 디코딩된(Decoded) 서비스키를 설정하세요.")
    private final String serviceKey;
    private final String baseUrl;
    private final int radius;
    private final int numOfRows;
    private final Duration requestInterval;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public TourApiProperties(String serviceKey, String baseUrl, int radius, int numOfRows,
                             Duration requestInterval, Duration connectTimeout, Duration readTimeout) {
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
        this.radius = radius;
        this.numOfRows = numOfRows;
        this.requestInterval = requestInterval;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
}
