package com.yagubogu.stat.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StatService {

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;

    public StatCountsResponse findStatCounts(final long memberId, final int year) {
        Member member = getById(memberId);
        validateUser(member);

        int winCounts = checkInRepository.findWinCounts(member, year);
        int drawCounts = checkInRepository.findDrawCounts(member, year);
        int loseCounts = checkInRepository.findLoseCounts(member, year);
        int favoriteCheckInCounts = winCounts + drawCounts + loseCounts;

        return new StatCountsResponse(winCounts, drawCounts, loseCounts, favoriteCheckInCounts);
    }

    public WinRateResponse findWinRate(final long memberId, final int year) {
        Member member = getById(memberId);
        validateUser(member);

        int winCounts = checkInRepository.findWinCounts(member, year);
        int drawCounts = checkInRepository.findDrawCounts(member, year);
        int loseCounts = checkInRepository.findLoseCounts(member, year);
        int favoriteCheckInCounts = winCounts + drawCounts + loseCounts;

        return new WinRateResponse(calculateWinRate(winCounts, favoriteCheckInCounts));
    }

    public LuckyStadiumResponse findLuckyStadium(final long memberId, final int year) {
        Member member = getById(memberId);
        validateUser(member);

        List<Stadium> stadiums = stadiumRepository.findAll();
        double lowestWinRate = 0;
        Stadium luckyStadium = null;
        for (Stadium stadium : stadiums) {
            int winCounts = checkInRepository.findWinCountsByStadiumAndMember(stadium, member, year);
            int favoriteCheckInCounts = checkInRepository.findFavoriteCheckInCountsByStadiumAndMember(stadium, member,
                    year);

            double currentWinRate = calculateWinRate(winCounts, favoriteCheckInCounts);
            if (currentWinRate > lowestWinRate) {
                lowestWinRate = currentWinRate;
                luckyStadium = stadium;
            }
        }
        return LuckyStadiumResponse.from(luckyStadium);
    }

    public AverageStatisticResponse findAverageStatistic(final long memberId) {
        Member member = getMember(memberId);
        AverageStatistic averageStatistic = checkInRepository.findAverageStatistic(member);

        return AverageStatisticResponse.from(averageStatistic);
    }

    private double calculateWinRate(final long winCounts, final long favoriteCheckInCounts) {
        if (favoriteCheckInCounts == 0) {
            return 0;
        }
        double rate = (double) winCounts / favoriteCheckInCounts * 100;
        return Math.round(rate * 10) / 10.0;
    }

    private Member getById(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private void validateUser(final Member member) {
        if (member.isAdmin()) {
            throw new ForbiddenException("Member should not be admin");
        }
    }
}
