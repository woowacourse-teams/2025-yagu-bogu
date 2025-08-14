package com.yagubogu.talk.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data-team-stadium.sql"
})
@Import(AuthTestConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
class TalkReportServiceTest {

    private TalkReportService talkReportService;

    @Autowired
    private TalkFactory talkFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TalkReportRepository talkReportRepository;

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        talkReportService = new TalkReportService(talkReportRepository, talkRepository, memberRepository);
    }

    @DisplayName("톡을 신고한다")
    @Test
    void reportTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member other = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk reportedTalk = talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when
        talkReportService.reportTalk(reportedTalk.getId(), me.getId());

        // then
        assertThat(talkReportRepository.existsByTalkIdAndReporterId(reportedTalk.getId(), me.getId())).isTrue();
    }

    @DisplayName("예외: 본인이 작성한 톡을 신고하면 예외가 발생한다")
    @Test
    void reportTalk_whenReportingOwnTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk myTalk = talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );

        // when & then
        assertThatThrownBy(() -> talkReportService.reportTalk(myTalk.getId(), me.getId()))
                .isExactlyInstanceOf(BadRequestException.class)
                .hasMessage("Cannot report your own comment");
    }

    @DisplayName("예외: 이미 신고한 톡을 재신고하면 예외가 발생한다")
    @Test
    void reportTalk_whenReportingAlreadyReportedTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member other = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk reportedTalk = talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        talkReportService.reportTalk(reportedTalk.getId(), me.getId());

        // when & then
        assertThatThrownBy(() -> talkReportService.reportTalk(reportedTalk.getId(), me.getId()))
                .isExactlyInstanceOf(BadRequestException.class)
                .hasMessage("You have already reported this talk");
    }
}
