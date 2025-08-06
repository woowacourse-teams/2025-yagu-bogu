package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.GamesResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.UnprocessableEntityException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GameService {

    private final GameRepository gameRepository;

    public GamesResponse findGamesByDate(final LocalDate date) {
        validateIsNotFuture(date);
        List<Game> games = gameRepository.findByDate(date);

        return GamesResponse.from(games);
    }

    private void validateIsNotFuture(final LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new UnprocessableEntityException("Cannot retrieve games for future dates");
        }
    }
}
