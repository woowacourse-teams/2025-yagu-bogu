package com.yagubogu.stat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.stadium.domain.QVictoryFairyRanking;
import com.yagubogu.team.domain.QTeam;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VictoryFairyRankingRepositoryImpl implements VictoryFairyRankingRepositoryCustom {

    private static final QTeam TEAM = QTeam.team;
    private static final QMember MEMBER = QMember.member;
    private static final QVictoryFairyRanking VICTORY_FAIRY_RANKING = QVictoryFairyRanking.victoryFairyRanking;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<VictoryFairyRank> findTopRankingByTeamFilterAndYear(final TeamFilter teamFilter, final int limit,
                                                                    final int year) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRank.class,
                                calculateRanking(VICTORY_FAIRY_RANKING.score),
                                VICTORY_FAIRY_RANKING.score,
                                MEMBER.nickname.value,
                                MEMBER.imageUrl,
                                TEAM.shortName
                        )
                ).from(VICTORY_FAIRY_RANKING)
                .join(VICTORY_FAIRY_RANKING.member, MEMBER)
                .join(MEMBER.team, TEAM)
                .where(
                        VICTORY_FAIRY_RANKING.gameYear.eq(year),
                        filterByTeam(teamFilter, TEAM),
                        MEMBER.deletedAt.isNull()
                )
                .orderBy(VICTORY_FAIRY_RANKING.score.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<VictoryFairyRank> findByMemberAndTeamFilterAndYear(
            final Member member,
            final TeamFilter teamFilter,
            final int year
    ) {
        QVictoryFairyRanking V2 = new QVictoryFairyRanking("v2");
        QMember M2 = new QMember("m2");
        QTeam T2 = new QTeam("t2");

        JPQLQuery<Integer> myRankingQuery = JPAExpressions
                .select(V2.count().add(1).intValue())
                .from(V2)
                .join(V2.member, M2)
                .join(M2.team, T2)
                .where(
                        V2.gameYear.eq(year),
                        filterByTeam(teamFilter, T2),
                        V2.score.gt(VICTORY_FAIRY_RANKING.score)
                );

        VictoryFairyRank victoryFairyRank = jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRank.class,
                                myRankingQuery,
                                VICTORY_FAIRY_RANKING.score,
                                MEMBER.nickname.value,
                                MEMBER.imageUrl,
                                TEAM.shortName
                        )
                ).from(VICTORY_FAIRY_RANKING)
                .join(VICTORY_FAIRY_RANKING.member, MEMBER)
                .join(MEMBER.team, TEAM)
                .where(
                        VICTORY_FAIRY_RANKING.gameYear.eq(year),
                        VICTORY_FAIRY_RANKING.member.eq(member),
                        filterByTeam(teamFilter, TEAM)
                )
                .orderBy(VICTORY_FAIRY_RANKING.score.desc())
                .fetchOne();

        return Optional.ofNullable(victoryFairyRank);
    }

    private BooleanExpression filterByTeam(final TeamFilter teamFilter, final QTeam team) {
        if (teamFilter == TeamFilter.ALL) {
            return null;
        }

        return team.teamCode.eq(teamFilter.name());
    }

    private NumberExpression<Integer> calculateRanking(NumberPath<Double> score) {
        return Expressions.numberTemplate(Integer.class,
                "RANK() OVER (ORDER BY {0} DESC)",  // DENSE_RANK() 대신 RANK()
                score);
    }
}
