package com.yagubogu.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@EnableConfigurationProperties(GoogleAuthProperties.class)
@Configuration
public class GoogleAuthClientConfig {

    private final GoogleAuthProperties googleAuthProperties;

    @Bean
    public RestClient googleRestClient(ClientHttpRequestFactory googleClientHttpRequestFactory) {
        return RestClient.builder()
                .baseUrl(googleAuthProperties.baseUri())
                .requestFactory(googleClientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory googleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) googleAuthProperties.connectTimeout().toMillis());
        factory.setReadTimeout((int) googleAuthProperties.readTimeout().toMillis());

        return factory;
    }
}
