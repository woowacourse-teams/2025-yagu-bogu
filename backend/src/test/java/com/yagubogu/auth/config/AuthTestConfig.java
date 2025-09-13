package com.yagubogu.auth.config;

import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.gateway.FakeAuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.QueryDslConfig;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.refreshtoken.RefreshTokenFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkReportFactory;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({AuthTokenProperties.class, GoogleAuthProperties.class})
@Import(QueryDslConfig.class)
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

    @Bean
    public GameFactory gameFactory(final GameRepository gameRepository) {
        return new GameFactory(gameRepository);
    }

    @Bean
    public CheckInFactory checkInFactory(final CheckInRepository checkInRepository) {
        return new CheckInFactory(checkInRepository);
    }

    @Bean
    public TalkFactory talkFactory(final TalkRepository talkRepository) {
        return new TalkFactory(talkRepository);
    }

    @Bean
    public TalkReportFactory talkReportFactory(final TalkReportRepository talkRepository) {
        return new TalkReportFactory(talkRepository);
    }

    @Bean
    public RefreshTokenFactory refreshTokenFactory(final RefreshTokenRepository refreshTokenRepository) {
        return new RefreshTokenFactory(refreshTokenRepository);
    }
}
