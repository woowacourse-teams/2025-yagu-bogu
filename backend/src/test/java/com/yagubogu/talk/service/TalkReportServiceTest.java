package com.yagubogu.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.member.repository.MemberRepository;
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
class TalkReportServiceTest {

    private TalkReportService talkReportService;

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
        long talkId = 11L;
        long reporterId = 2L;

        // when
        talkReportService.reportTalk(talkId, reporterId);

        // then
        assertSoftly(softAssertions -> {
            assertThat(talkReportRepository.count()).isEqualTo(1);
            assertThat(talkReportRepository.existsByTalkIdAndReporterId(talkId, reporterId)).isTrue();
        });
    }

    @DisplayName("예외: 본인이 작성한 톡을 신고하면 예외가 발생한다")
    @Test
    void reportTalk_whenReportingOwnTalk() {
        // given
        long talkId = 11L;
        long reporterId = 1L;

        // when & then
        assertThatThrownBy(() -> talkReportService.reportTalk(talkId, reporterId))
                .isExactlyInstanceOf(BadRequestException.class)
                .hasMessage("Cannot report your own comment");
    }

    @DisplayName("예외: 이미 신고한 톡을 재신고하면 예외가 발생한다")
    @Test
    void reportTalk_whenReportingAlreadyReportedTalk() {
        // given
        long talkId = 11L;
        long reporterId = 2L;

        // when & then
        talkReportService.reportTalk(talkId, reporterId);
        assertThatThrownBy(() -> talkReportService.reportTalk(talkId, reporterId))
                .isExactlyInstanceOf(BadRequestException.class)
                .hasMessage("You have already reported this talk");
    }
}
