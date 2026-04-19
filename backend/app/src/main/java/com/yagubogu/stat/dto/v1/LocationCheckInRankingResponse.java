package com.yagubogu.stat.dto.v1;

import com.yagubogu.checkin.dto.LocationCheckInRankParam;
import com.yagubogu.member.domain.Member;
import java.util.List;

public record LocationCheckInRankingResponse(
        List<LocationCheckInRankingEntry> topRankings,
        LocationCheckInRankingEntry myRanking
) {

    public record LocationCheckInRankingEntry(
            long ranking,
            long memberId,
            String nickname,
            String profileImageUrl,
            String teamShortName,
            long visitCount
    ) {

        public static LocationCheckInRankingEntry emptyRanking(final Member member) {
            return new LocationCheckInRankingEntry(
                    0,
                    member.getId(),
                    member.getNickname().toString(),
                    member.getImageUrl(),
                    member.getTeam() != null ? member.getTeam().getShortName() : null,
                    0
            );
        }

        public static LocationCheckInRankingEntry from(final long ranking, final LocationCheckInRankParam param) {
            return new LocationCheckInRankingEntry(
                    ranking,
                    param.memberId(),
                    param.nickname(),
                    param.profileImageUrl(),
                    param.teamShortName(),
                    param.visitCount()
            );
        }
    }

    public static LocationCheckInRankingResponse of(
            final List<LocationCheckInRankParam> topRankings,
            final LocationCheckInRankingEntry myRanking
    ) {
        List<LocationCheckInRankingEntry> entries = topRankings.stream()
                .map(param -> LocationCheckInRankingEntry.from(
                        topRankings.indexOf(param) + 1L,
                        param
                ))
                .toList();

        return new LocationCheckInRankingResponse(entries, myRanking);
    }
}
