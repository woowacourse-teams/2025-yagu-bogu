package yagubogu.crawling.game.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kbo")
@Getter
@Setter
@Slf4j
public class KboCrawlerProperties {

    private CrawlerConfig crawler;
    private CrawlerSchedulerProperties scheduler;
    private PerGameRetryProperties retry;
    private Selectors selectors;
    private Patterns patterns;

    @Getter
    @Setter
    public static class CrawlerConfig {

        @NotBlank
        private String scoreBoardUrl;
        @NotBlank
        private String gameCenterUrl;
        @NotNull
        private Duration navigationTimeout;
        @NotNull
        private Duration tableTimeout;

        private int maxRetries;
        @NotNull
        private Duration retryDelay;
        @NotNull
        private Duration waitTimeout;
        @NotBlank
        private String dateFieldSelector;
        @NotBlank
        private String dateLabelSelector;
        @NotBlank
        private String updatePanelSelector;
        @NotBlank
        private String scoreSelector;
        private int contextPoolSize;
    }

    @Getter
    @Setter
    public static class Selectors {
        private CalendarSelectors calendar;
        private ScoreboardSelectors scoreboard;
    }

    @Getter
    @Setter
    public static class CalendarSelectors {
        private String trigger;
        private String container;
        private String yearSelect;
        private String monthSelect;
        private String dayLink;
        private String dateLabel;
        private String updatePanel;
    }

    @Getter
    @Setter
    public static class ScoreboardSelectors {
        private String container;
        private String status;
        private String stadium;
        private String startTime;
        private TeamSelectors awayTeam;
        private TeamSelectors homeTeam;
        private String boxScoreLink;
        private ScoreTableSelectors scoreTable;
        private PitcherSelectors pitcher;
    }

    @Getter
    @Setter
    public static class TeamSelectors {
        private String name;
        private String score;
    }

    @Getter
    @Setter
    public static class ScoreTableSelectors {
        private String table;
        private String rows;
        private String teamName;
        private String cells;
    }

    @Getter
    @Setter
    public static class PitcherSelectors {
        private String container;
        private String spans;
    }

    @Getter
    @Setter
    public static class Patterns {
        private String pitcherLabel;
        private String dateFormat;
        private String timeFormat;
    }
}
