package yagubogu.crawling.game.service.poller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import yagubogu.crawling.game.config.CrawlerSchedulerProperties;

class PollingIntervalTest {

    @DisplayName("메인 폴링 루프는 경기별 폴링 주기 설정을 공유한다")
    @Test
    void sharePollingIntervalWithGameSchedule() throws NoSuchMethodException {
        Scheduled scheduled = AdaptivePoller.class
                .getDeclaredMethod("pollGameWhenReachDue")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled.fixedDelayString())
                .isEqualTo("${kbo.scheduler.polling-interval:15s}");
    }

    @DisplayName("다음 경기 폴링은 기본값인 15초 뒤에 실행한다")
    @Test
    void scheduleNextPollAfterFifteenSeconds() {
        Instant now = Instant.parse("2026-08-29T10:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        GameScheduleManager scheduleManager = new GameScheduleManager(
                clock,
                new CrawlerSchedulerProperties()
        );

        scheduleManager.scheduleNextPoll(1L);

        assertThat(scheduleManager.shouldPollGame(1L, now.plusSeconds(14))).isFalse();
        assertThat(scheduleManager.shouldPollGame(1L, now.plusSeconds(15))).isTrue();
    }
}
