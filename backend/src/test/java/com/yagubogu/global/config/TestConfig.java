package com.yagubogu.global.config;

import com.yagubogu.auth.service.GoogleAuthValidator;
import com.yagubogu.auth.token.JwtProvider;
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
}
