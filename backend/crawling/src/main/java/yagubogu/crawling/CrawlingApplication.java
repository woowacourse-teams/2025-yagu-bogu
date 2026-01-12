package yagubogu.crawling;

import com.yagubogu.global.config.ClockConfig;
import com.yagubogu.global.config.PipelineConfig;
import com.yagubogu.global.config.QueryDslConfig;
import com.yagubogu.global.config.ScheduleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import({
        ClockConfig.class,
        QueryDslConfig.class,
        ScheduleConfig.class,
        PipelineConfig.class
})
@EntityScan(basePackages = "com.yagubogu")
@EnableJpaRepositories(basePackages = "com.yagubogu")
@SpringBootApplication
public class CrawlingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlingApplication.class, args);
    }
}
