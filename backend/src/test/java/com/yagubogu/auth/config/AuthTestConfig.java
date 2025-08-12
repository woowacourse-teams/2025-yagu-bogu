package com.yagubogu.auth.config;

import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.gateway.FakeAuthGateway;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.member.MemberFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties({AuthTokenProperties.class, GoogleAuthProperties.class})
public class AuthTestConfig {

    @Bean
    public AuthTokenProvider authTokenProvider(AuthTokenProperties authTokenProperties) {
        return new AuthTokenProvider(authTokenProperties);
    }

    @Bean
    public GoogleAuthValidator googleAuthValidator(final GoogleAuthProperties googleAuthProperties) {
        return new GoogleAuthValidator(googleAuthProperties);
    }

    @Bean
    public AuthGateway authGateway() {
        return new FakeAuthGateway();
    }

    @Bean
    public AuthFactory authFactory(AuthTokenProvider authTokenProvider) {
        return new AuthFactory(authTokenProvider);
    }

    @Bean
    public MemberFactory memberFactory(MemberRepository memberRepository) {
        return new MemberFactory(memberRepository);
    }
}
