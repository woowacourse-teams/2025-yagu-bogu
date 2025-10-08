package com.yagubogu.stat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.stadium.domain.QVictoryFairyRanking;
import com.yagubogu.team.domain.QTeam;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VictoryFairyRankingRepositoryImpl implements VictoryFairyRankingRepositoryCustom {

    private static final QTeam TEAM = QTeam.team;
    private static final QMember MEMBER = QMember.member;
    private static final QVictoryFairyRanking VICTORY_FAIRY_RANKING = QVictoryFairyRanking.victoryFairyRanking;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<VictoryFairyRank> findTopByTeamFilter(final TeamFilter teamFilter, final int limit, final int year) {
        return jpaQueryFactory.selectFrom(
                        calculateRanking(),
                        VICTORY_FAIRY_RANKING
                )
                .join(VICTORY_FAIRY_RANKING.member, MEMBER).fetchJoin()
                .join(MEMBER.team, TEAM).fetchJoin()
                .where(
                        VICTORY_FAIRY_RANKING.gameYear.eq(year),
                        filterByTeam(teamFilter)
                )
                .orderBy(VICTORY_FAIRY_RANKING.score.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression filterByTeam(final TeamFilter teamFilter) {
        if (teamFilter == TeamFilter.ALL) {
            return null;
        }

        return QTeam.team.teamCode.eq(teamFilter.name());
    }
}
