package yagubogu.crawling.game.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ClockConfig {

    @Value("${app.clock.fixed-date:}")
    private String fixedDate;

    @Bean
    @Profile("!prod")
    public Clock clock() {
        if (fixedDate != null && !fixedDate.isEmpty()) {
            LocalDate date = LocalDate.parse(fixedDate);
            return Clock.fixed(
                    date.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
                    ZoneId.of("Asia/Seoul")
            );
        }
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
