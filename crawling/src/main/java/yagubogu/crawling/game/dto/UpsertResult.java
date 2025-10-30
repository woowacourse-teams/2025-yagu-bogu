package yagubogu.crawling.game.dto;

import java.util.List;

public record UpsertResult(int successCount, List<FailedGame> failedGames) {
}
