package com.yagubogu.fixture;

import java.time.LocalDate;

public class TestFixture {

    public static LocalDate getToday() {
        return LocalDate.of(2025, 7, 21);
    }

    public static LocalDate getInvalidDate() {
        return LocalDate.of(1000, 6, 15);
    }
}
