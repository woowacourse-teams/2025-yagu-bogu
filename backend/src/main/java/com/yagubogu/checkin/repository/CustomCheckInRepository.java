package com.yagubogu.checkin.repository;

public interface CustomCheckInRepository {

    double calculateTotalAverageWinRate(int year);

    double calculateAverageCheckInCount(int year);
}
