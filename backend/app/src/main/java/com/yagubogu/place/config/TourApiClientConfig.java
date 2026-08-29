package com.yagubogu.place.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.place.client.TourApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TourApiProperties.class)
public class TourApiClientConfig {

    @Bean
    public RestClient tourApiRestClient(TourApiProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) props.getReadTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public TourApiClient tourApiClient(RestClient tourApiRestClient,
                                       TourApiProperties props,
                                       ObjectMapper objectMapper) {
        return new TourApiClient(tourApiRestClient, props, objectMapper);
    }
}
