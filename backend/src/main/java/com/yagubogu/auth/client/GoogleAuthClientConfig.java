package com.yagubogu.auth.client;

import com.yagubogu.global.config.GoogleAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GoogleAuthProperties.class)
public class GoogleAuthClientConfig {

    private final GoogleAuthProperties googleAuthProperties;

    public GoogleAuthClientConfig(final GoogleAuthProperties googleAuthProperties) {
        this.googleAuthProperties = googleAuthProperties;
    }

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return (restClientBuilder) -> {
            restClientBuilder.requestFactory(clientHttpRequestFactory())
                    .baseUrl(googleAuthProperties.getBaseUri());
        };
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(30000);
        return factory;
    }
}
