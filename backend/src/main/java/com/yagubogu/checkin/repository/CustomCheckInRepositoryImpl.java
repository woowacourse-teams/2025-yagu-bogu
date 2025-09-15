package com.yagubogu.checkin.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.team.domain.QTeam;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomCheckInRepositoryImpl implements CustomCheckInRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    // m : 전체 유저 평균 승롤 (전체 완료된 경기의 인증 중 승수 / 전체 완료된 경기의 인증수)
    public double calculateTotalAverageWinRate(int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        NumberExpression<Long> isWin = calculateWinCounts(checkIn, game);

        Tuple tuple = jpaQueryFactory.select(isWin.sum(), checkIn.count())
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(isFavoriteTeam(checkIn, member)).fetchOne();

        long winCounts =
                (tuple == null || tuple.get(0, Long.class) == null) ? 0L : tuple.get(0, Long.class).longValue();
        long totalCounts = (tuple == null || tuple.get(1, Long.class) == null) ? 0L : tuple.get(1, Long.class);

        return (totalCounts == 0) ? 0.5 : (double) winCounts / totalCounts;
    }

    @Override
    // c : 전체 유저 직관 횟수 평균
    public double calculateAverageCheckInCount(int year) {
        // 전체 유저 직관 횟수
        Long totalCheckInCount = calculateTotalCheckInCount(year);
        // 직관을 간 유저 숫자
        Long perCheckInCount = calculatePerCheckInCount(year);

        return (perCheckInCount == 0) ? 0.0 : (double) totalCheckInCount / perCheckInCount;
    }

    @Override
    public List<VictoryFairyRank> findTopVictoryRanking(final double m, final double c, final int year,
                                                        final TeamFilter teamFilter, final int limit) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        NumberExpression<Long> wins = calculateWinCounts(checkIn, game);
        NumberExpression<Long> total = game.id.count();
        NumberExpression<Double> score = calculateWinRankingScore(m, c, wins, total);

        NumberExpression<Double> calculatePercent = wins.sum().multiply(100.0).divide(total).doubleValue();
        NumberExpression<Double> safeWinPercent = getSafeWinPercent(total, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRank.class, score, member.nickname, member.imageUrl,
                                member.team.shortName, safeWinPercent))
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(filterByTeam(member.team, teamFilter))
                .groupBy(member.id, member.nickname, member.imageUrl, member.team.shortName)
                .orderBy(score.desc()).limit(limit).fetch();
    }

    public VictoryFairyRank findMyRanking(final double m, final double c, final Member targetMember, final int year,
                                          final TeamFilter teamFilter) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        // w: 개인의 승리 횟수
        NumberExpression<Long> wins = calculateWinCounts(checkIn, game);
        NumberExpression<Long> total = game.id.count().coalesce(0L);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, wins, total);

        NumberExpression<Double> calculatePercent = wins.sum().multiply(100.0).divide(total).doubleValue();

        NumberExpression<Double> safeWinPercent = getSafeWinPercent(total, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(VictoryFairyRank.class, score, member.nickname, member.imageUrl,
                                member.team.shortName, safeWinPercent))
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(member.eq(targetMember), filterByTeam(member.team, teamFilter))
                .groupBy(member.id, member.nickname, member.imageUrl, member.team.shortName)
                .fetchOne();
    }

    private NumberExpression<Double> getSafeWinPercent(final NumberExpression<Long> total,
                                                       final NumberExpression<Double> calculatePercent) {
        return new CaseBuilder().when(total.gt(0)).then(calculatePercent).otherwise(0.0);
    }

    public int calculateMyRankingOrder(final double targetScore, final double m, final double c, final int year,
                                       final TeamFilter teamFilter) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        // w: 개인의 승리 횟수
        NumberExpression<Long> wins = calculateWinCounts(checkIn, game);
        NumberExpression<Long> total = checkIn.count();
        NumberExpression<Double> score = calculateWinRankingScore(m, c, wins, total);

        Expression<Double> myScore = Expressions.constant(targetScore);
        return jpaQueryFactory.selectOne()
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game))
                .where(isFinished(game), isBetweenYear(game, year), filterByTeam(team, teamFilter))
                .groupBy(member)
                .having(score.gt(myScore))
                .fetch().size();
    }

    private NumberExpression<Double> calculateWinRankingScore(final double m, final double c,
                                                              final NumberExpression<Long> wins,
                                                              final NumberExpression<Long> total) {
        NumberExpression<Double> denominator = total.doubleValue().add(Expressions.constant(c));

        return new CaseBuilder().when(denominator.ne(0.0))
                .then(wins.sum().doubleValue().add(Expressions.constant(c * m)).divide(denominator)).otherwise(0.0);
    }

    private BooleanExpression isFinished(final QGame game) {
        return game.gameState.eq(GameState.COMPLETED);
    }

    private BooleanExpression isBetweenYear(QGame game, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return game.date.between(start, end);
    }

    private BooleanExpression filterByTeam(final QTeam team, TeamFilter teamFilter) {
        if (teamFilter == TeamFilter.ALL) {
            return Expressions.TRUE.isTrue();
        }

        return team.teamCode.eq(teamFilter.name());
    }

    private Long calculateTotalCheckInCount(int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        return jpaQueryFactory.select(checkIn.count()).from(checkIn).join(game).on(checkIn.game.eq(game)).join(member)
                .on(checkIn.member.eq(member))
                .where(isFinished(game), isBetweenYear(game, year), isFavoriteTeam(checkIn, member)).fetchOne();
    }

    private NumberExpression<Long> calculateWinCounts(final QCheckIn checkIn, final QGame game) {
        return new CaseBuilder()
                .when(checkIn.team.eq(game.homeTeam).and(game.homeScore.gt(game.awayScore)))
                .then(1L)
                .when(checkIn.team.eq(game.awayTeam).and(game.awayScore.gt(game.homeScore)))
                .then(1L)
                .otherwise(0L);
    }

    private Long calculatePerCheckInCount(final int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;
        return jpaQueryFactory.select(member.countDistinct())
                .from(checkIn)
                .join(game).on(checkIn.game.eq(game))
                .join(member).on(checkIn.member.eq(member))
                .where(isFinished(game), isBetweenYear(game, year), isFavoriteTeam(checkIn, member))
                .fetchOne();
    }

    private BooleanExpression isFavoriteTeam(final QCheckIn checkIn, final QMember member) {
        return checkIn.team.eq(member.team);
    }
}
