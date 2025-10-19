package yagubogu.crawling.game.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kbo.crawler")
@Getter
@Setter
public class KboCrawlerProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String scheduleUrl;

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

    @NotBlank
    private String eventTarget;

    private int contextPoolSize;

    private final Runner runner = new Runner();

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
