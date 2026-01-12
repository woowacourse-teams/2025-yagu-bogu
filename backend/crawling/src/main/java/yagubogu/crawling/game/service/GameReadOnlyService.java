package yagubogu.crawling.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameReadOnlyService {

    private final GameRepository gameRepository;

    public List<Game> findAllByDate(final LocalDate date) {
        return gameRepository.findAllByDate(date);
    }

    public List<Game> findAllByDateWithStadium(final LocalDate date) {
        return gameRepository.findAllByDateWithStadium(date);
    }

    public boolean existsByDateAndGameStateIn(final LocalDate date, final List<GameState> states) {
        return gameRepository.existsByDateAndGameStateIn(date, states);
    }
}
