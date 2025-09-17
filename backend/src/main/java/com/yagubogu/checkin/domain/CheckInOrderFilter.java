package com.yagubogu.checkin.domain;

public enum CheckInOrderFilter {
    LATEST,
    OLDEST,
    ;

    public boolean isOldest() {
        return this == OLDEST;
    }
}
