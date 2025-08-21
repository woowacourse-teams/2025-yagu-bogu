package com.yagubogu.talk.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkReportFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.TalkCursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
class TalkServiceTest {

    private TalkService talkService;

    @Autowired
    private TalkFactory talkFactory;

    @Autowired
    private TalkReportFactory talkReportFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TalkReportRepository talkReportRepository;

    @BeforeEach
    void setUp() {
        talkService = new TalkService(talkRepository, gameRepository, memberRepository, talkReportRepository);
    }

    @DisplayName("최신 커서가 없는 경우 첫 페이지를 조회한다 - 다음 페이지가 없는 경우")
    @Test
    void findFirstPage_whenNoCursor_noNextPage() {
        // given
        int limit = 10;
        Long cursorId = null;
        Member firstEnterMember = memberFactory.save(MemberBuilder::build);

        Stadium expectedStadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team expectedHomeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team expectedAwayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(expectedHomeTeam)
                .awayTeam(expectedAwayTeam)
                .stadium(expectedStadium));

        Member expectedMessageWriter = memberFactory.save(builder -> builder.team(expectedAwayTeam));
        Talk expectedTalk = talkFactory.save(builder ->
                builder.member(expectedMessageWriter)
                        .game(game)
        );

        // when
        TalkCursorResult actual = talkService.findTalksExcludingReported(
                game.getId(),
                cursorId,
                limit,
                firstEnterMember.getId()
        );

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.stadiumName()).isEqualTo(expectedStadium.getFullName());
            softAssertions.assertThat(actual.homeTeamName()).isEqualTo(expectedHomeTeam.getShortName());
            softAssertions.assertThat(actual.awayTeamName()).isEqualTo(expectedAwayTeam.getShortName());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().id()).isEqualTo(expectedTalk.getId());
            softAssertions.assertThat(actual.cursorResult().content().size()).isOne();
            softAssertions.assertThat(actual.cursorResult().content().getFirst().memberId())
                    .isEqualTo(expectedTalk.getMember().getId());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().imageUrl())
                    .isEqualTo(expectedTalk.getMember().getImageUrl());
            softAssertions.assertThat(actual.cursorResult().nextCursorId()).isNull();
            softAssertions.assertThat(actual.cursorResult().hasNext()).isFalse();
        });
    }

    @DisplayName("최신 커서가 없는 경우 첫 페이지를 조회한다 - 다음 페이지가 있는 경우")
    @Test
    void findFirstPage_whenNoCursor_nextPageExists() {
        // given
        int limit = 1;
        Long cursorId = null;
        Member firstEnterMember = memberFactory.save(MemberBuilder::build);

        Stadium expectedStadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team expectedHomeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team expectedAwayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(expectedHomeTeam)
                .awayTeam(expectedAwayTeam)
                .stadium(expectedStadium));

        Member expectedMessageWriter = memberFactory.save(builder -> builder.team(expectedAwayTeam));
        Talk expectedFirstPageTalk = talkFactory.save(builder ->
                builder.member(expectedMessageWriter)
                        .game(game)
        );
        Talk expectedSecondPageTalk = talkFactory.save(builder ->
                builder.member(expectedMessageWriter)
                        .game(game)
        );

        // when
        TalkCursorResult actual = talkService.findTalksExcludingReported(
                game.getId(),
                cursorId,
                limit,
                firstEnterMember.getId()
        );

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.stadiumName()).isEqualTo(expectedStadium.getFullName());
            softAssertions.assertThat(actual.homeTeamName()).isEqualTo(expectedHomeTeam.getShortName());
            softAssertions.assertThat(actual.awayTeamName()).isEqualTo(expectedAwayTeam.getShortName());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().id())
                    .isEqualTo(expectedSecondPageTalk.getId());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().memberId())
                    .isEqualTo(expectedFirstPageTalk.getMember().getId());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().imageUrl())
                    .isEqualTo(expectedFirstPageTalk.getMember().getImageUrl());
            softAssertions.assertThat(actual.cursorResult().nextCursorId()).isEqualTo(expectedSecondPageTalk.getId());
            softAssertions.assertThat(actual.cursorResult().hasNext()).isTrue();
        });
    }

    @DisplayName("위로 슬라이드 하는 과정에서 이전 채팅 기록 가져오기")
    @Test
    void findFirstPage_whenCursorExists_returnsPreviousTalks() {
        // given
        int limit = 1;
        Long cursorId = null;
        Member firstEnterMember = memberFactory.save(MemberBuilder::build);

        Stadium expectedStadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team expectedHomeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team expectedAwayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(expectedHomeTeam)
                .awayTeam(expectedAwayTeam)
                .stadium(expectedStadium));

        Member expectedMessageWriter = memberFactory.save(builder -> builder.team(expectedAwayTeam));
        Talk expectedFirstPageTalk = talkFactory.save(builder ->
                builder.member(expectedMessageWriter)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(expectedMessageWriter)
                        .game(game)
        );

        TalkCursorResult result = talkService.findTalksExcludingReported(
                game.getId(),
                cursorId,
                limit,
                firstEnterMember.getId()
        );

        // when
        TalkCursorResult actual = talkService.findTalksExcludingReported(
                game.getId(),
                result.cursorResult().nextCursorId(),
                limit,
                firstEnterMember.getId()
        );

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.stadiumName()).isEqualTo(expectedStadium.getFullName());
            softAssertions.assertThat(actual.homeTeamName()).isEqualTo(expectedHomeTeam.getShortName());
            softAssertions.assertThat(actual.awayTeamName()).isEqualTo(expectedAwayTeam.getShortName());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().id())
                    .isEqualTo(expectedFirstPageTalk.getId());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().memberId())
                    .isEqualTo(expectedFirstPageTalk.getMember().getId());
            softAssertions.assertThat(actual.cursorResult().content().getFirst().imageUrl())
                    .isEqualTo(expectedFirstPageTalk.getMember().getImageUrl());
            softAssertions.assertThat(actual.cursorResult().nextCursorId()).isNull();
            softAssertions.assertThat(actual.cursorResult().hasNext()).isFalse();
        });
    }

//    @DisplayName("위로 슬라이드 하는 과정에서 이전 채팅 기록 가져오기 - 소프트 딜리트")
//    @Test
//    void findFirstPage_whenCursorExists_returnsLeftMember_softDelete() {
//        // given
//        int limit = 2;
//        Long cursorId = null;
//        Member firstEnterMember = memberFactory.save(MemberBuilder::build);
//
//        Stadium expectedStadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
//        Team expectedHomeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
//        Team expectedAwayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
//        Game game = gameFactory.save(builder -> builder.homeTeam(expectedHomeTeam)
//                .awayTeam(expectedAwayTeam)
//                .stadium(expectedStadium));
//
//        Member expectedMessageWriter = memberFactory.save(builder -> builder.team(expectedAwayTeam));
//        Talk expectedFirstPageTalk = talkFactory.save(builder ->
//                builder.member(expectedMessageWriter)
//                        .game(game)
//        );
//        Member soonLeftMember = memberFactory.save(MemberBuilder::build);
//        Talk remainedTalkByLeftMember = talkFactory.save(builder ->
//                builder.member(soonLeftMember)
//                        .game(game)
//        );
//        memberRepository.delete(soonLeftMember);
//
//        // when
//        TalkCursorResult result = talkService.findTalksExcludingReported(
//                game.getId(),
//                cursorId,
//                limit,
//                firstEnterMember.getId()
//        );
//
//        // then
//        assertSoftly(softAssertions -> {
//            softAssertions.assertThat(result.stadiumName()).isEqualTo(expectedStadium.getFullName());
//            softAssertions.assertThat(result.homeTeamName()).isEqualTo(expectedHomeTeam.getShortName());
//            softAssertions.assertThat(result.awayTeamName()).isEqualTo(expectedAwayTeam.getShortName());
//            softAssertions.assertThat(result.cursorResult().content().getFirst().id())
//                    .isEqualTo(remainedTalkByLeftMember.getId());
//            softAssertions.assertThat(result.cursorResult().content().getFirst().memberId())
//                    .isEqualTo(remainedTalkByLeftMember.getMember().getId());
//            softAssertions.assertThat(result.cursorResult().content().getFirst().imageUrl())
//                    .isEqualTo(remainedTalkByLeftMember.getMember().getImageUrl());
//            softAssertions.assertThat(result.cursorResult().nextCursorId()).isNull();
//            softAssertions.assertThat(result.cursorResult().hasNext()).isFalse();
//        });
//    }

    @DisplayName("가져온 톡 중 자신이 작성한 톡을 구분할 수 있다")
    @Test
    void findTalks_myTalk() {
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
        Long cursorId = 5L;
        int limit = 4;
        long expectedMyTalkCount = 3L;

        talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when
        TalkCursorResult actual = talkService.findTalksExcludingReported(
                game.getId(),
                cursorId,
                limit,
                me.getId()
        );
        long actualMyTalkCount = actual.cursorResult()
                .content()
                .stream()
                .filter(TalkResponse::isMine)
                .count();

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actualMyTalkCount).isEqualTo(expectedMyTalkCount);
        });
    }

    @DisplayName("새로운 메시지가 있을 때 polling으로 가져온다")
    @Test
    void findNewTalks_hasNewTalk() {
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

        Long cursorId = 2L;
        int limit = 2;

        talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );
        Talk thirdTalk = talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        Talk fourthTalk = talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when
        TalkCursorResult actual = talkService.findNewTalks(
                game.getId(),
                cursorId,
                me.getId(),
                limit
        );

        List<TalkResponse> expectedCursorResult = List.of(
                TalkResponse.from(fourthTalk, me.getId()),
                TalkResponse.from(thirdTalk, me.getId())
        );

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.stadiumName()).isEqualTo(thirdTalk.getGame().getStadium().getFullName());
            softAssertions.assertThat(actual.homeTeamName())
                    .isEqualTo(thirdTalk.getGame().getHomeTeam().getShortName());
            softAssertions.assertThat(actual.awayTeamName())
                    .isEqualTo(thirdTalk.getGame().getAwayTeam().getShortName());
            softAssertions.assertThat(actual.cursorResult().content()).hasSize(expectedCursorResult.size());
            softAssertions.assertThat(actual.cursorResult().content()).containsExactlyElementsOf(expectedCursorResult);
            softAssertions.assertThat(actual.cursorResult().nextCursorId()).isEqualTo(thirdTalk.getId());
            softAssertions.assertThat(actual.cursorResult().hasNext()).isFalse();
        });
    }

    @DisplayName("새 메시지가 없을 때 nextCursorId는 바뀌지 않는다")
    @Test
    void findNewTalks_hasNoNewTalk() {
        // given
        int limit = 1;

        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk fristTalk = talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );

        // when
        TalkCursorResult actual = talkService.findNewTalks(game.getId(), fristTalk.getId(), me.getId(), limit);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.cursorResult().content()).isEmpty();
            softAssertions.assertThat(actual.cursorResult().nextCursorId()).isEqualTo(fristTalk.getId());
            softAssertions.assertThat(actual.cursorResult().hasNext()).isFalse();
        });
    }

    @DisplayName("새로운 톡을 생성한다")
    @Test
    void createTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        String content = "오늘 야구 재밌겠당";
        TalkRequest request = new TalkRequest(content);

        // when
        TalkResponse response = talkService.createTalk(game.getId(), request, me.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.content()).isEqualTo(content);
            softAssertions.assertThat(response.memberId()).isEqualTo(me.getId());
            softAssertions.assertThat(response.id()).isEqualTo(1L);
        });
    }

    @DisplayName("예외: 특정 횟수 이상 신고를 당했다면 새로운 톡을 생성할 때 예외가 발생한다")
    @Test
    void createTalk_blockedFromStadium() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member kindMember1 = memberFactory.save(MemberBuilder::build);
        Member kindMember2 = memberFactory.save(MemberBuilder::build);
        Member kindMember3 = memberFactory.save(MemberBuilder::build);
        Member kindMember4 = memberFactory.save(MemberBuilder::build);
        Member kindMember5 = memberFactory.save(MemberBuilder::build);
        Member kindMember6 = memberFactory.save(MemberBuilder::build);
        Member kindMember7 = memberFactory.save(MemberBuilder::build);
        Member kindMember8 = memberFactory.save(MemberBuilder::build);
        Member kindMember9 = memberFactory.save(MemberBuilder::build);
        Member kindMember10 = memberFactory.save(MemberBuilder::build);

        Member blockedMember = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk blockedTalk = talkFactory.save(builder ->
                builder.member(blockedMember)
                        .game(game)
        );

        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember1)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember2)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember3)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember4)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember5)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember6)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember7)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember8)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember9)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember10)
        );

        String content = "오늘 야구 재밌겠당";
        TalkRequest request = new TalkRequest(content);

        // when & then
        assertThatThrownBy(() -> talkService.createTalk(game.getId(), request, blockedMember.getId()))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot chat due to multiple user reports");
    }

    @DisplayName("본인이 작성한 톡을 삭제한다")
    @Test
    void removeTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk talk = talkFactory.save(builder ->
                builder.member(member)
                        .game(game)
        );

        // when
        talkService.removeTalk(game.getId(), talk.getId(), member.getId());

        // then
        assertThat(talkRepository.findById(talk.getId())).isNotPresent();
    }

    @DisplayName("본인이 작성한 톡을 삭제한다 - 소프트 딜리트 검증")
    @Test
    void removeTalk_softDelete() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk talk = talkFactory.save(builder ->
                builder.member(member)
                        .game(game)
        );

        // when
        talkService.removeTalk(game.getId(), talk.getId(), member.getId());

        // then
        assertThat(talkRepository.findById(talk.getId())).isEmpty();
    }

    @DisplayName("예외: 다른 사람이 작성한 톡을 삭제하려고 하면 예외가 발생한다")
    @Test
    void removeTalk_ByOtherUser() {
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

        Talk myTalk = talkFactory.save(builder ->
                builder.member(me)
                        .game(game)
        );

        // when & then
        assertThatThrownBy(() -> talkService.removeTalk(game.getId(), myTalk.getId(), other.getId()))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid memberId for the talk");
    }
}
