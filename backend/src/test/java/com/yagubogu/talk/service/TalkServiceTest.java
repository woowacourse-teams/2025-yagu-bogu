package com.yagubogu.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:talk-test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
class TalkServiceTest {

    private TalkService talkService;

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

    @DisplayName("최신 커서가 없는 경우 첫 페이지를 조회한다")
    @Test
    void findTalks_firstPage() {
        // given
        long gameId = 1L;
        Long cursorId = null;
        int limit = 10;
        long memberId = 1L;

        long expectedLatestTalkId = 52L;
        long expectedMemberId = 2L;
        String expectedImageUrl = "https://image.com/fivera.png";
        Long expectedNextCursorId = 43L;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalksExcludingReported(gameId, cursorId, limit,
                memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
            softAssertions.assertThat(actual.content().getFirst().memberId()).isEqualTo(expectedMemberId);
            softAssertions.assertThat(actual.content().getFirst().imageUrl()).isEqualTo(expectedImageUrl);
            softAssertions.assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
            softAssertions.assertThat(actual.hasNext()).isTrue();
        });
    }

    @DisplayName("커서가 주어진 경우 해당 커서 다음 페이지를 조회한다")
    @Test
    void findTalks_middlePage() {
        // given
        long gameId = 1L;
        Long cursorId = 43L;
        int limit = 10;
        long memberId = 1L;

        long expectedLatestTalkId = 42L;
        long expectedMemberId = 2L;
        String expectedImageUrl = "https://image.com/fivera.png";
        Long expectedNextCursorId = 33L;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalksExcludingReported(gameId, cursorId, limit,
                memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
            softAssertions.assertThat(actual.content().getFirst().memberId()).isEqualTo(expectedMemberId);
            softAssertions.assertThat(actual.content().getFirst().imageUrl()).isEqualTo(expectedImageUrl);
            softAssertions.assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
            softAssertions.assertThat(actual.hasNext()).isTrue();
        });
    }

    @DisplayName("마지막 페이지 조회 시 hasNext가 false이고 nextCursorId가 null이다")
    @Test
    void findTalks_lastPage() {
        // given
        long gameId = 1L;
        Long cursorId = 3L;
        int limit = 10;
        long memberId = 1L;

        long expectedLatestTalkId = 2L;
        long expectedMemberId = 2L;
        String expectedImageUrl = "https://image.com/fivera.png";
        Long expectedNextCursorId = null;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalksExcludingReported(gameId, cursorId, limit,
                memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
            softAssertions.assertThat(actual.content().getFirst().memberId()).isEqualTo(expectedMemberId);
            softAssertions.assertThat(actual.content().getFirst().imageUrl()).isEqualTo(expectedImageUrl);
            softAssertions.assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
            softAssertions.assertThat(actual.hasNext()).isFalse();
        });
    }

    @DisplayName("가져온 톡 중 자신이 작성한 톡을 구분할 수 있다")
    @Test
    void findTalks_myTalk() {
        // given
        long gameId = 1L;
        Long cursorId = 43L;
        int limit = 20;
        long memberId = 1L;

        long expectedMyTalkCount = 10L;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalksExcludingReported(gameId, cursorId, limit,
                memberId);
        long actualMyTalkCount = actual.content().stream()
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
        long gameId = 1L;
        Long cursorId = 50L;
        long memberId = 1L;
        int limit = 10;

        long expectedLastTalkId = 52L;
        Long expectedNextCursorId = 52L;

        // when
        CursorResult<TalkResponse> actual = talkService.findNewTalks(gameId, cursorId, memberId, limit);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.content()).hasSize(2);
            softAssertions.assertThat(actual.content().getLast().id()).isEqualTo(expectedLastTalkId);
            softAssertions.assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
            softAssertions.assertThat(actual.hasNext()).isFalse();
        });
    }

    @DisplayName("새 메시지가 없을 때 nextCursorId는 바뀌지 않는다")
    @Test
    void findNewTalks_hasNoNewTalk() {
        // given
        long gameId = 1L;
        Long cursorId = 52L;
        long memberId = 1L;
        int limit = 10;

        // when
        CursorResult<TalkResponse> actual = talkService.findNewTalks(gameId, cursorId, memberId, limit);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.content()).isEmpty();
            softAssertions.assertThat(actual.nextCursorId()).isEqualTo(cursorId);
            softAssertions.assertThat(actual.hasNext()).isFalse();
        });
    }

    @DisplayName("새로운 톡을 생성한다")
    @Test
    void createTalk() {
        // given
        long gameId = 1L;
        long memberId = 1L;
        String content = "오늘 야구 재밌겠당";
        TalkRequest request = new TalkRequest(content);

        // when
        TalkResponse response = talkService.createTalk(gameId, request, memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.content()).isEqualTo(content);
            softAssertions.assertThat(response.memberId()).isEqualTo(memberId);
            softAssertions.assertThat(response.id()).isEqualTo(53L);
        });
    }

    @DisplayName("예외: 특정 횟수 이상 신고를 당했다면 새로운 톡을 생성할 때 예외가 발생한다")
    @Test
    void createTalk_blockedFromStadium() {
        // given
        long gameId = 1L;
        long blockedMemberId = 2L;
        String content = "오늘 야구 재밌겠당";
        TalkRequest request = new TalkRequest(content);

        // when & then
        assertThatThrownBy(() -> talkService.createTalk(gameId, request, blockedMemberId))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot chat due to multiple user reports");
    }

    @DisplayName("본인이 작성한 톡을 삭제한다")
    @Test
    void removeTalk() {
        // given
        long gameId = 1L;
        long talkId = 9L;
        long memberId = 1L;

        // pre-condition check
        assertThat(talkRepository.findById(talkId)).isPresent();

        // when
        talkService.removeTalk(gameId, talkId, memberId);

        // then
        assertThat(talkRepository.findById(talkId)).isNotPresent();
    }

    @DisplayName("예외: 다른 사람이 작성한 톡을 삭제하려고 하면 예외가 발생한다")
    @Test
    void removeTalk_ByOtherUser() {
        // given
        long gameId = 1L;
        long talkId = 10L;
        long memberId = 1L;

        // pre-condition check
        assertThat(talkRepository.findById(talkId)).isPresent();

        // when & then
        assertThatThrownBy(() -> talkService.removeTalk(gameId, talkId, memberId))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid memberId for the talk");
    }
}
