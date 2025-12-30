package yagubogu.crawling.game.dto;

import java.util.List;

public record BatchResult(int success, List<Integer> failedIndices, long tookMs) {
    public static BatchResult empty() {
        return new BatchResult(0, List.of(), 0);
    }

    public boolean hasFailures() {
        return !failedIndices.isEmpty();
    }
}
