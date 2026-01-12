package yagubogu.crawling.game.dto;

import com.yagubogu.game.domain.Game;
import java.util.List;

public record GamesFromGameCenter(
        List<Game> games
) {
}
