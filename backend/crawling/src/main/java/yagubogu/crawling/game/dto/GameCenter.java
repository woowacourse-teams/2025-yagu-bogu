package yagubogu.crawling.game.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class GameCenter {

    private String date;
    private List<GameCenterDetail> games = new ArrayList<>();

    public void addGameDetail(GameCenterDetail game) {
        this.games.add(game);
    }
}
