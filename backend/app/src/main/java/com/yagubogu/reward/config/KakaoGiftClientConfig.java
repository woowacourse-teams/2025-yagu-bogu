package com.yagubogu.reward.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(KakaoGiftProperties.class)
@Configuration
public class KakaoGiftClientConfig {

    @Bean
    public RestClient kakaoGiftRestClient(
            final KakaoGiftProperties properties,
            @Qualifier("kakaoGiftRequestFactory") final ClientHttpRequestFactory requestFactory
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory kakaoGiftRequestFactory(final KakaoGiftProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(toTimeoutMillis(properties.connectTimeout()));
        factory.setReadTimeout(toTimeoutMillis(properties.readTimeout()));
        return factory;
    }

    private static int toTimeoutMillis(final Duration timeout) {
        long millis = timeout.toMillis();
        if (millis < 0 || millis > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Kakao Gift timeout must be between 0ms and Integer.MAX_VALUE ms"
            );
        }
        return Math.toIntExact(millis);
    }
}
