package com.yagubogu.stat.repository;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.InsertDto;
import com.yagubogu.stat.dto.UpdateDto;
import java.util.List;
import java.util.Optional;

public interface VictoryFairyRankingRepositoryCustom {

    Optional<VictoryFairyRankParam> findByMemberAndTeamFilterAndYear(Member member, TeamFilter teamFilter, int year);

    Optional<Long> findRankWithinTeamByMemberAndYear(Member member, int year);

    void batchUpdate(List<UpdateDto> updates, int batchSize);

    void batchInsert(List<InsertDto> toInsert, int batchSize);

    void nonBatchUpdate(List<UpdateDto> updates);

    void nonBatchInsert(List<InsertDto> toInsert);
}
