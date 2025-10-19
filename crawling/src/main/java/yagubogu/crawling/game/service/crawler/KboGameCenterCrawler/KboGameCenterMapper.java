package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.yagubogu.game.domain.GameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KboGameCenterMapper {

    public GameState toState(String statusText) {
        if (statusText == null || statusText.isBlank()) {
            return GameState.SCHEDULED;
        }

        return switch (statusText.trim()) {
            case "경기예정" -> GameState.SCHEDULED;
            case "경기취소" -> GameState.CANCELED;
            case "경기종료" -> GameState.COMPLETED;
            case "경기중" -> GameState.LIVE;  // 혹시 있을 경우
            default -> {
                log.warn("알 수 없는 상태값: {}", statusText);
                yield GameState.SCHEDULED;
            }
        };
    }
}
