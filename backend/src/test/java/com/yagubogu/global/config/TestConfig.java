package com.yagubogu.global.config;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.client.FakeAuthGateway;
import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.config.JwtProperties;
import com.yagubogu.auth.service.GoogleAuthValidator;
import com.yagubogu.auth.service.JwtProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties({JwtProperties.class, GoogleAuthProperties.class})
public class TestConfig {

    @Bean
    public JwtProvider jwtProvider(JwtProperties jwtProperties) {
        return new JwtProvider(jwtProperties);
    }

    @Bean
    public GoogleAuthValidator googleAuthValidator(final GoogleAuthProperties googleAuthProperties) {
        return new GoogleAuthValidator(googleAuthProperties);
    }

    @Bean
    public AuthGateway authGateway(){
        return new FakeAuthGateway();
    }
}
