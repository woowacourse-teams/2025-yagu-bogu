package com.yagubogu.game.dto;

import java.math.BigDecimal;

public record TeamWinRateRow(int rank, String team, int totalPlays, int winCounts, int loseCounts, int drawCounts,
                             BigDecimal winRate, double diffRate, String latestPlays, String streak,
                             String home, String away) {
}
