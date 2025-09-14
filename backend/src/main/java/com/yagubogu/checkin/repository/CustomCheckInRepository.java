package com.yagubogu.checkin.repository;

import com.yagubogu.member.domain.Member;

public interface CustomCheckInRepository {

    int findWinCounts(Member member, final int year);

    int findLoseCounts(Member member, final int year);

    int findDrawCounts(Member member, final int year);

    int findRecentTenGamesWinCounts(Member member, final int year);

    int findRecentTenGamesLoseCounts(Member member, final int year);

    int findRecentTenGamesDrawCounts(Member member, final int year);
}
