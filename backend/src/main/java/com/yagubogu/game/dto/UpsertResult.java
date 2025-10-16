package com.yagubogu.game.dto;

import java.util.List;

public record UpsertResult(int successCount, List<FailedGame> failedGames) {
}
