package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import yagubogu.crawling.game.dto.GameDetailInfo;

/**
 * 일일 경기 데이터 컨테이너
 */
@Data
public class DailyGameData {
    private String date;
    private List<GameDetailInfo> games = new ArrayList<>();

    public void addGameDetail(GameDetailInfo game) {
        this.games.add(game);
    }
}
