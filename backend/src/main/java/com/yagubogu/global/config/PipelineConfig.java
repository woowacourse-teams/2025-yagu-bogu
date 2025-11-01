package com.yagubogu.global.config;

import com.yagubogu.game.repository.BronzeGameRepository;
import com.yagubogu.game.service.BronzeGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@Slf4j
public class PipelineConfig {

    @Bean
    public BronzeGameService bronzeGameService(final BronzeGameRepository bronzeGameRepository) {
        return new BronzeGameService(bronzeGameRepository);
    }
}
