package com.yagubogu.game.service.crawler.KboWinRateCrawler;

public enum SeriesType {
    REGULAR("0"),
    EXHIBITION("1");

    private final String value;

    SeriesType(String v) {
        this.value = v;
    }

    public String value() {
        return value;
    }
}
