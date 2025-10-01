package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import com.yagubogu.game.service.crawler.config.KboCrawlerProperties;
import com.yagubogu.game.service.crawler.config.KboCrawlerProperties.Runner;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "kbo.crawler.runner", name = "enabled", havingValue = "true")
public class KboScheduleCrawlerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KboScheduleCrawlerRunner.class);

    private final KboScheduleCrawler crawler;
    private final KboCrawlerProperties properties;

    public KboScheduleCrawlerRunner(final KboScheduleCrawler crawler,
                                    final KboCrawlerProperties properties) {
        this.crawler = crawler;
        this.properties = properties;
    }

    @Override
    public void run(final ApplicationArguments args) {
        Runner runner = properties.getRunner();
        LocalDate startDate = Objects.requireNonNull(runner.getStartDate(),
                "kbo.crawler.runner.start-date 속성은 필수입니다.");
        LocalDate endDate = Objects.requireNonNull(runner.getEndDate(),
                "kbo.crawler.runner.end-date 속성은 필수입니다.");
        String scheduleType = Objects.requireNonNullElse(runner.getScheduleType(), "regular");

        log.info("KBO 일정 크롤링 실행 - 기간: {} ~ {}, 시즌: {}", startDate, endDate, scheduleType);
        List<KboGame> games = crawler.crawlKboSchedule(startDate, endDate, scheduleType, log);
        log.info("수집된 경기 수: {}", games.size());

        for (KboGame game : games) {
            log.info("- {} {} vs {} (경기장: {}, 취소: {}, 비고: {})",
                    game.getDate(),
                    game.getHomeTeam(),
                    game.getAwayTeam(),
                    game.getStadium(),
                    game.isCancelled() ? "Y" : "N",
                    game.getCancelReason());
        }
    }
}
