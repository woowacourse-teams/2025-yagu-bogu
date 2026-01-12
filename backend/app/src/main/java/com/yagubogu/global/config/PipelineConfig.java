package com.yagubogu.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.repository.BronzeGameRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
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

    @Bean
    public GameEtlService gameEtlService(final BronzeGameRepository bronzeGameRepository,
                                         final GameRepository gameRepository, final TeamRepository teamRepository,
                                         final StadiumRepository stadiumRepository, final ObjectMapper objectMapper,
                                         final Clock clock
    ) {
        return new GameEtlService(bronzeGameRepository, gameRepository, teamRepository, stadiumRepository,
                objectMapper, clock);
    }
}
