package com.yagubogu.stat.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.InsertDto;
import com.yagubogu.stat.dto.UpdateDto;
import java.util.List;
import java.util.Optional;

public interface VictoryFairyRankingRepositoryCustom {

    List<VictoryFairyRank> findTopRankingByTeamFilterAndYear(TeamFilter teamFilter, int limit, int year);

    Optional<VictoryFairyRank> findByMemberAndTeamFilterAndYear(Member member, TeamFilter teamFilter, int year);

    void batchUpdate(List<UpdateDto> updates, int batchSize);

    void batchInsert(List<InsertDto> toInsert, int batchSize);
}
