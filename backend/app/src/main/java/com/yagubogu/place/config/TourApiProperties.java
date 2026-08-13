package com.yagubogu.place.config;

import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "tour.api")
public class TourApiProperties {

    private final String serviceKey;
    private final String baseUrl;
    private final int radius;
    private final int numOfRows;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public TourApiProperties(String serviceKey, String baseUrl, int radius, int numOfRows,
                             Duration connectTimeout, Duration readTimeout) {
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
        this.radius = radius;
        this.numOfRows = numOfRows;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
}
