package yagubogu.crawling.game.service.crawler.KboScheduleCrawler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum ScheduleType {

    ALL, REGULAR, POSTSEASON, TRIAL, NONE;

    public static final Map<ScheduleType, List<String>> SERIES_IDS = Map.of(
            REGULAR, List.of("0", "9", "6"),
            POSTSEASON, List.of("3", "4", "5", "7"),
            TRIAL, List.of("1")
    );

    private static final Map<Integer, Set<ScheduleType>> DEFAULT_RULE = Map.of(
            2, Set.of(TRIAL),
            3, new LinkedHashSet<>(List.of(TRIAL, REGULAR)),
            4, Set.of(REGULAR),
            5, Set.of(REGULAR),
            6, Set.of(REGULAR),
            7, Set.of(REGULAR),
            8, Set.of(REGULAR),
            9, Set.of(REGULAR),
            10, new LinkedHashSet<>(List.of(REGULAR, POSTSEASON)),
            11, Set.of(POSTSEASON)
    );

    public static Set<ScheduleType> resolveTypesForMonth(final int month) {
        return DEFAULT_RULE.getOrDefault(month, Set.of(NONE));
    }

    public String getSeriesParam() {
        return String.join(",", ScheduleType.SERIES_IDS.get(this));
    }
}
