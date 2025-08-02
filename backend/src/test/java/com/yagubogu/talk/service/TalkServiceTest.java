package com.yagubogu.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        long expectedLatestTalkId = 52L;
        Long expectedNextCursorId = 43L;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
        assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
        assertThat(actual.hasNext()).isTrue();
    }

    @DisplayName("커서가 주어진 경우 해당 커서 다음 페이지를 조회한다")
    @Test
    void findTalks_middlePage() {
        // given
        long gameId = 1L;
        Long cursorId = 43L;
        int limit = 10;

        long expectedLatestTalkId = 42L;
        Long expectedNextCursorId = 33L;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
        assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
        assertThat(actual.hasNext()).isTrue();
    }

    @DisplayName("마지막 페이지 조회 시 hasNext가 false이고 nextCursorId가 null이다")
    @Test
    void findTalks_lastPage() {
        // given
        long gameId = 1L;
        Long cursorId = 3L;
        int limit = 10;

        long expectedLatestTalkId = 2L;
        Long expectedNextCursorId = null;

        // when
        CursorResult<TalkResponse> actual = talkService.findTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content().getFirst().id()).isEqualTo(expectedLatestTalkId);
        assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
        assertThat(actual.hasNext()).isFalse();
    }

    @DisplayName("새로운 메시지가 있을 때 polling으로 가져온다")
    @Test
    void pollTalks_hasNewTalk() {
        // given
        long gameId = 1L;
        Long cursorId = 50L;
        int limit = 10;

        long expectedLastTalkId = 52L;
        Long expectedNextCursorId = 52L;

        // when
        CursorResult<TalkResponse> actual = talkService.pollTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content()).hasSize(2);
        assertThat(actual.content().getLast().id()).isEqualTo(expectedLastTalkId);
        assertThat(actual.nextCursorId()).isEqualTo(expectedNextCursorId);
        assertThat(actual.hasNext()).isFalse();
    }

    @DisplayName("새 메시지가 없을 때 nextCursorId는 바뀌지 않는다")
    @Test
    void pollTalks_hasNoNewTalk() {
        // given
        long gameId = 1L;
        Long cursorId = 52L;
        int limit = 10;

        // when
        CursorResult<TalkResponse> actual = talkService.pollTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content()).isEmpty();
        assertThat(actual.nextCursorId()).isEqualTo(cursorId);
        assertThat(actual.hasNext()).isFalse();
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
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.id()).isEqualTo(53L);
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