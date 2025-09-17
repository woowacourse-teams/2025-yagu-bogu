package com.yagubogu.checkin.repository;

import com.yagubogu.member.domain.Member;

public interface CustomCheckInRepository {

    int findWinCounts(Member member, final int year);

    int findLoseCounts(Member member, final int year);

    int findDrawCounts(Member member, final int year);

    int findRecentGamesWinCounts(Member member, final int year, final int limit);

    int findRecentGamesLoseCounts(Member member, final int year, final int limit);

    int findRecentGamesDrawCounts(Member member, final int year, final int limit);
}
