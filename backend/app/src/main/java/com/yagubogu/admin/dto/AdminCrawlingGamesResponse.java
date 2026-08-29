package com.yagubogu.admin.dto;

import java.util.List;

public record AdminCrawlingGamesResponse(
        int requested,
        int saved,
        int skipped,
        int transformed,
        int reviewSaved,
        int reviewQueued,
        int failed,
        List<String> savedGameCodes,
        List<String> failedGameCodes,
        List<String> failedDates
) {
}
