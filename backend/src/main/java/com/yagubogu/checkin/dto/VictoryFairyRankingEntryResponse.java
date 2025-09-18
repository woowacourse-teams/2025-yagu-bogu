package com.yagubogu.checkin.dto;

import com.yagubogu.member.domain.Member;

public record VictoryFairyRankingEntryResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        String teamShortName,
        Long totalCheckIns,
        double winPercent
) {

    public static VictoryFairyRankingEntryResponse generateEmptyRankingFor(final Member member) {
        return new VictoryFairyRankingEntryResponse(
                member.getId(),
                member.getNickname().getValue(),
                member.getImageUrl(),
                member.getTeam().getShortName(),
                0L,
                0.0
        );
    }
}
