package com.yagubogu.talk.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties =
        "spring.sql.init.data-locations=classpath:talk-test-data.sql"
)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
class TalkServiceTest {

    private TalkService talkService;

    @Autowired
    private TalkRepository talkRepository;

    @BeforeEach
    void setUp() {
        talkService = new TalkService(talkRepository);
    }

    @DisplayName("최신 커서가 없는 경우 첫 페이지를 조회한다")
    @Test
    void findTalks_firstPage() {
        // given
        Long gameId = 1L;
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
        Long gameId = 1L;
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
        Long gameId = 1L;
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
        Long gameId = 1L;
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
        Long gameId = 1L;
        Long cursorId = 52L;
        int limit = 10;

        // when
        CursorResult<TalkResponse> actual = talkService.pollTalks(gameId, cursorId, limit);

        // then
        assertThat(actual.content()).isEmpty();
        assertThat(actual.nextCursorId()).isEqualTo(cursorId);
        assertThat(actual.hasNext()).isFalse();
    }
}