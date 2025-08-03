package com.yagubogu.auth.client;

import com.yagubogu.global.config.GoogleAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(GoogleAuthProperties.class)
@Configuration
public class GoogleAuthClientConfig {

    private final GoogleAuthProperties googleAuthProperties;

    public GoogleAuthClientConfig(final GoogleAuthProperties googleAuthProperties) {
        this.googleAuthProperties = googleAuthProperties;
    }

    @Bean
    public RestClient googleRestClient(ClientHttpRequestFactory clientHttpRequestFactory) {
        return RestClient.builder()
                .baseUrl(googleAuthProperties.getBaseUri())
                .requestFactory(clientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(30000);

        return factory;
    }
}
