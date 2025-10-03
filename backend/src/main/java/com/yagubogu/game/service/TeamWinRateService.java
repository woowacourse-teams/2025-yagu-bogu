package com.yagubogu.game.service;

import com.yagubogu.game.dto.TeamWinRateResponse;
import com.yagubogu.game.dto.TeamWinRateRow;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.KboTeamWinRateCrawler;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.SeriesType;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TeamWinRateService {

    private final TeamRepository teamRepository;
    private final KboTeamWinRateCrawler kboTeamWinRateCrawler;

    public TeamWinRateResponse fetchTeamWinRates(
            final SeriesType seriesType
    ) {
        Set<String> validTeams = teamRepository.findAll().stream()
                .flatMap(team -> createTeamVariants(team).stream())
                .collect(Collectors.toSet());

        List<TeamWinRateRow> winRates = kboTeamWinRateCrawler.crawl(validTeams, seriesType);

        return new TeamWinRateResponse(winRates);
    }

    private List<String> createTeamVariants(final Team team) {
        return Stream.of(team.getName(), team.getShortName())
                .flatMap(name -> Stream.of(name, name.replace(" ", "")))
                .distinct()
                .toList();
    }
}
