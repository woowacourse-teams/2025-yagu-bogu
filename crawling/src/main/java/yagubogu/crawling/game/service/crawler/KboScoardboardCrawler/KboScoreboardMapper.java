package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import com.yagubogu.game.domain.GameState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class KboScoreboardMapper {

    /**
     * "경기전", "경기중", "경기종료" 등 상태 문자열을 GameState로 매핑
     */
    GameState toState(String statusText, Integer homeRuns, Integer awayRuns) {
        if (statusText == null) {
            return GameState.SCHEDULED;
        }
        String s = statusText.trim();
        if (s.contains("전")) {
            return GameState.SCHEDULED;
        }
        if (s.contains("중")) {
            return GameState.LIVE;
        }
        if (s.contains("종료") || s.contains("끝")) {
            return GameState.COMPLETED;
        }

        // 점수 유무로 보정
        if (homeRuns != null || awayRuns != null) {
            return GameState.LIVE;
        }
        return GameState.SCHEDULED;
    }
}

