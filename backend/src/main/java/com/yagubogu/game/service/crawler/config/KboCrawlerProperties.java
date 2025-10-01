package com.yagubogu.game.service.crawler.config;

import java.time.Duration;
import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kbo.crawler")
public class KboCrawlerProperties {

    private Duration navigationTimeout = Duration.ofSeconds(60);
    private Duration tableTimeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofSeconds(2);
    private final Runner runner = new Runner();

    public Duration getNavigationTimeout() {
        return navigationTimeout;
    }

    public void setNavigationTimeout(final Duration navigationTimeout) {
        this.navigationTimeout = navigationTimeout;
    }

    public Duration getTableTimeout() {
        return tableTimeout;
    }

    public void setTableTimeout(final Duration tableTimeout) {
        this.tableTimeout = tableTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(final Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Runner getRunner() {
        return runner;
    }

    public static class Runner {
        private boolean enabled;
        private String scheduleType = "regular";
        private LocalDate startDate;
        private LocalDate endDate;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getScheduleType() {
            return scheduleType;
        }

        public void setScheduleType(final String scheduleType) {
            this.scheduleType = scheduleType;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(final LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(final LocalDate endDate) {
            this.endDate = endDate;
        }
    }
}
