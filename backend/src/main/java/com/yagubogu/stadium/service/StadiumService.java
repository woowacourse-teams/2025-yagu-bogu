package com.yagubogu.stadium.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.v1.StadiumsWithGamesResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StadiumService {

    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;

    public StadiumsWithGamesResponse findWithGameByDate(final LocalDate date) {
        List<Game> games = gameRepository.findByDateWithStadium(date);
        Map<Stadium, List<Game>> gamesByStadium = games
                .stream()
                .collect(Collectors.groupingBy(Game::getStadium));

        return StadiumsWithGamesResponse.from(gamesByStadium);
    }
}
