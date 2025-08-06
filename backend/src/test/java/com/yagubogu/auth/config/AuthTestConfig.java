package com.yagubogu.auth.config;

import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.gateway.FakeAuthGateway;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.auth.support.AuthTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties({AuthTokenProperties.class, GoogleAuthProperties.class})
public class AuthTestConfig {

    @Bean
    public AuthTokenProvider jwtProvider(AuthTokenProperties authTokenProperties) {
        return new AuthTokenProvider(authTokenProperties);
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
