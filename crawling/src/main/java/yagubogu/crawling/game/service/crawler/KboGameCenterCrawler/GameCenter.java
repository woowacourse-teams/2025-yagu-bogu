package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Data
public class GameCenter {

    private String date;
    private List<GameCenterDetail> games = new ArrayList<>();

    public void addGameDetail(GameCenterDetail game) {
        this.games.add(game);
    }
}
