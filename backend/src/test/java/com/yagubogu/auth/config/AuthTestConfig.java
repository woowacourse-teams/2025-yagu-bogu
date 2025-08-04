package com.yagubogu.auth.config;

import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.gateway.FakeAuthGateway;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.auth.support.JwtProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties({JwtProperties.class, GoogleAuthProperties.class})
public class AuthTestConfig {

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
