package com.yagubogu.auth.config;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(AppleAuthProperties.class)
@Configuration
public class AppleAuthClientConfig {

    @Bean
    public JwkProvider appleJwkProvider(final AppleAuthProperties appleAuthProperties) throws MalformedURLException {
        URL jwksUrl = new URL(appleAuthProperties.jwksUri());
        return new JwkProviderBuilder(jwksUrl)
                .cached(5, 6, TimeUnit.HOURS)
                .build();
    }
}
