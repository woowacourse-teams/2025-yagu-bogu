package com.yagubogu.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.repository.BronzeGameRepository;
import com.yagubogu.game.repository.GameHitterRecordRepository;
import com.yagubogu.game.repository.GamePitcherRecordRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.game.service.GameReviewService;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;

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
                                         final Clock clock, final TransactionTemplate transactionTemplate
    ) {
        return new GameEtlService(bronzeGameRepository, gameRepository, teamRepository, stadiumRepository,
                objectMapper, clock, transactionTemplate);
    }

    @Bean
    public GameReviewService gameReviewService(final GameRepository gameRepository,
                                               final GameHitterRecordRepository hitterRecordRepository,
                                               final GamePitcherRecordRepository pitcherRecordRepository) {
        return new GameReviewService(gameRepository, hitterRecordRepository, pitcherRecordRepository);
    }
}
