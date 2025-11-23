package com.yagubogu.stat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.stat.domain.QVictoryFairyRanking;
import com.yagubogu.stat.dto.InsertDto;
import com.yagubogu.stat.dto.UpdateDto;
import com.yagubogu.team.domain.QTeam;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class VictoryFairyRankingRepositoryImpl implements VictoryFairyRankingRepositoryCustom {

    private static final QTeam TEAM = QTeam.team;
    private static final QMember MEMBER = QMember.member;
    private static final QVictoryFairyRanking VICTORY_FAIRY_RANKING = QVictoryFairyRanking.victoryFairyRanking;

    private final JPAQueryFactory jpaQueryFactory;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<VictoryFairyRankParam> findByMemberAndTeamFilterAndYear(
            final Member member,
            final TeamFilter teamFilter,
            final int year
    ) {
        QVictoryFairyRanking V2 = new QVictoryFairyRanking("v2");
        QMember M2 = new QMember("m2");
        QTeam T2 = new QTeam("t2");

        JPQLQuery<Long> myRankingQuery = JPAExpressions
                .select(V2.count().add(1))
                .from(V2)
                .join(V2.member, M2)
                .join(M2.team, T2)
                .where(
                        V2.gameYear.eq(year),
                        filterByTeam(teamFilter, T2),
                        V2.score.gt(VICTORY_FAIRY_RANKING.score)
                );

        VictoryFairyRankParam victoryFairyRank = jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRankParam.class,
                                myRankingQuery,
                                MEMBER.id,
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

    @Override
    public void batchUpdate(List<UpdateDto> updates, int batchSize) {
        jdbcTemplate.batchUpdate(
                """
                        UPDATE victory_fairy_rankings 
                        SET score = ?, 
                            win_count = ?, 
                            check_in_count = ?, 
                            updated_at = NOW()
                        WHERE victory_fairy_ranking_id = ?
                        """,
                updates,
                batchSize,
                (ps, dto) -> {
                    ps.setDouble(1, dto.score());
                    ps.setInt(2, dto.winCount());
                    ps.setInt(3, dto.checkInCount());
                    ps.setLong(4, dto.id());
                }
        );
    }

    @Override
    public void batchInsert(final List<InsertDto> inserts, final int batchSize) {
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO victory_fairy_rankings 
                        (member_id, game_year, score, win_count, check_in_count, updated_at)
                        VALUES (?, ?, ?, ?, ?, NOW())
                        """,
                inserts,
                batchSize,
                (ps, dto) -> {
                    ps.setLong(1, dto.memberId());
                    ps.setInt(2, dto.year());
                    ps.setDouble(3, dto.score());
                    ps.setInt(4, dto.winCount());
                    ps.setInt(5, dto.checkInCount());
                }
        );
    }

    @Override
    public void nonBatchUpdate(final List<UpdateDto> updates) {
        String sql = """
                UPDATE victory_fairy_rankings 
                SET score = ?, 
                    win_count = ?, 
                    check_in_count = ?, 
                    updated_at = NOW()
                WHERE victory_fairy_ranking_id = ?
                """;

        for (UpdateDto dto : updates) {
            jdbcTemplate.update(
                    sql,
                    dto.score(),
                    dto.winCount(),
                    dto.checkInCount(),
                    dto.id()
            );
        }
    }

    @Override
    public void nonBatchInsert(final List<InsertDto> toInsert) {
        String sql = """
                INSERT INTO victory_fairy_rankings 
                (member_id, game_year, score, win_count, check_in_count, updated_at)
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        for (InsertDto dto : toInsert) {
            jdbcTemplate.update(
                    sql,
                    dto.memberId(),
                    dto.year(),
                    dto.score(),
                    dto.winCount(),
                    dto.checkInCount()
            );
        }
    }

    @Override
    public Optional<Long> findRankWithinTeamByMemberAndYear(final Member member, final int year) {
        // 1. 멤버에게 응원팀이 없으면 랭킹 계산 불가
        if (member.getTeam() == null) {
            return Optional.empty();
        }

        // 2. 해당 연도에 멤버의 랭킹 기록(점수) 조회
        Double memberScore = jpaQueryFactory
                .select(VICTORY_FAIRY_RANKING.score)
                .from(VICTORY_FAIRY_RANKING)
                .where(
                        VICTORY_FAIRY_RANKING.member.eq(member),
                        VICTORY_FAIRY_RANKING.gameYear.eq(year)
                )
                .fetchOne();

        // 3. 랭킹 기록이 없으면 팀 내 순위도 없음
        if (memberScore == null) {
            return Optional.empty();
        }

        // 4. 나보다 점수가 높은 '같은 팀' 멤버의 수 조회
        QVictoryFairyRanking ranking = QVictoryFairyRanking.victoryFairyRanking;
        QMember qMember = QMember.member;

        // Long으로 받아서 Optional로 감싼 후, null일 경우 0L을 기본값으로 사용
        long higherRankedCount = Optional.ofNullable(jpaQueryFactory
                .select(ranking.count())
                .from(ranking)
                .join(ranking.member, qMember)
                .where(
                        qMember.team.eq(member.getTeam()),
                        ranking.gameYear.eq(year),
                        ranking.score.gt(memberScore)
                )
                .fetchOne()).orElse(0L);

        long rank = higherRankedCount + 1;

        return Optional.of(rank);
    }

    private BooleanExpression filterByTeam(final TeamFilter teamFilter, final QTeam team) {
        if (teamFilter == TeamFilter.ALL) {
            return null;
        }

        return team.teamCode.eq(teamFilter.name());
    }
}
