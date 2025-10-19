package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import yagubogu.crawling.game.dto.GameDetailInfo;

@Data
public class DailyGameData {

    private String date;
    private List<GameDetailInfo> games = new ArrayList<>();

    public void addGameDetail(GameDetailInfo game) {
        this.games.add(game);
    }
}
