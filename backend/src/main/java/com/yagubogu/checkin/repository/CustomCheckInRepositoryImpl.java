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

    /**
     * m : 전체 유저 평균 승률
     *
     * 정의: 특정 연도의 모든 "완료된 경기" 기준으로,
     *       각 유저의 인증(CheckIn) 중에서 승리한 횟수 / 전체 인증 횟수
     *
     * 계산식:
     *   - 분자: 승리한 인증 수 (유저가 응원한 팀이 이긴 경우)
     *   - 분모: 전체 인증 수
     *   - 전체 유저를 합산하여 평균 승률을 반환
     *
     * @param year 기준 연도
     * @return 전체 유저 평균 승률 (0.0 ~ 1.0). 인증이 없으면 0.5 기본값 반환
     */
    @Override
    public double calculateTotalAverageWinRate(int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        NumberExpression<Long> w = calculateWinCounts(checkIn, game);

        Tuple tuple = jpaQueryFactory.select(w, checkIn.count())
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(
                        isFavoriteTeam(checkIn, member),
                        isMemberNotDeleted(member)
                ).fetchOne();

        long winCounts =
                (tuple == null || tuple.get(0, Long.class) == null) ? 0L : tuple.get(0, Long.class).longValue();
        long totalCounts = (tuple == null || tuple.get(1, Long.class) == null) ? 0L : tuple.get(1, Long.class);

        return (totalCounts == 0) ? 0.0 : (double) winCounts / totalCounts;
    }

    /**
     * c : 전체 유저 평균 직관 횟수
     *
     * 정의: 특정 연도의 총 직관(체크인) 횟수 / 직관에 참여한 유저 수
     *
     * 계산식:
     *   - 분자: 해당 연도의 모든 인증 기록 수 (ΣN)
     *   - 분모: 해당 연도에 한 번이라도 직관한 유저 수
     *
     * @param year 기준 연도
     * @return 전체 유저 평균 직관 횟수 (0.0 이상)
     */
    @Override
    public double calculateAverageCheckInCount(int year) {
        // 전체 유저 직관 횟수
        Long totalCheckInCount = calculateTotalCheckInCount(year);
        // 직관을 간 유저 숫자
        Long perCheckInCount = calculatePerCheckInCount(year);

        return (perCheckInCount == 0) ? 0.0 : (double) totalCheckInCount / perCheckInCount;
    }

    /**
     * 전체 유저 중 "승리 요정 랭킹" 상위 N명을 조회
     *
     * - 베이즈 평균 공식을 기반으로 점수(score)를 계산
     *   score = (W + C * m) / (N + C)
     *     W: 개인 승리 횟수
     *     N: 개인 직관 수
     *     m: 전체 평균 승률
     *     C: 전체 평균 직관 횟수
     *
     * @param m 전체 유저 평균 승률
     * @param c 전체 유저 평균 직관 횟수
     * @param year 기준 연도
     * @param teamFilter 팀 필터(ALL 또는 특정 팀만 조회)
     * @param limit 상위 N명 제한
     * @return VictoryFairyRank DTO 리스트 (상위 랭킹)
     */
    @Override
    public List<VictoryFairyRank> findTopVictoryRanking(final double m, final double c, final int year,
                                                        final TeamFilter teamFilter, final int limit) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        NumberExpression<Long> w = calculateWinCounts(checkIn, game);
        NumberExpression<Long> total = calculateTotalCountsWithoutDraws(game);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, total);

        NumberExpression<Double> calculatePercent = w.multiply(100.0).divide(total).doubleValue();
        NumberExpression<Double> safeWinPercent = getSafeWinPercent(total, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRank.class, score, member.nickname, member.imageUrl,
                                member.team.shortName, safeWinPercent))
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(
                        filterByTeam(member.team, teamFilter),
                        isMemberNotDeleted(member)
                )
                .groupBy(member.id, member.nickname, member.imageUrl, member.team.shortName)
                .orderBy(score.desc()).limit(limit).fetch();
    }

    /**
     * 특정 유저의 승리 요정 랭킹 점수 및 정보 조회
     *
     * @param m 전체 유저 평균 승률
     * @param c 전체 유저 평균 직관 횟수
     * @param targetMember 조회 대상 유저
     * @param year 기준 연도
     * @param teamFilter 팀 필터
     * @return VictoryFairyRank DTO (단일 유저의 랭킹 정보)
     */
    public VictoryFairyRank findMyRanking(final double m, final double c, final Member targetMember, final int year,
                                          final TeamFilter teamFilter) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        NumberExpression<Long> w = calculateWinCounts(checkIn, game);
        NumberExpression<Long> n = calculateTotalCountsWithoutDraws(game);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, n);

        NumberExpression<Double> calculatePercent = w.multiply(100.0).divide(n).doubleValue();

        NumberExpression<Double> safeWinPercent = getSafeWinPercent(n, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(VictoryFairyRank.class, score, member.nickname, member.imageUrl,
                                member.team.shortName, safeWinPercent))
                .from(member)
                .leftJoin(checkIn).on(checkIn.member.eq(member))
                .leftJoin(game).on(checkIn.game.eq(game), isFinished(game), isBetweenYear(game, year))
                .where(
                        member.eq(targetMember),
                        filterByTeam(member.team, teamFilter),
                        isMemberNotDeleted(member)
                )
                .groupBy(member.id, member.nickname, member.imageUrl, member.team.shortName)
                .fetchOne();
    }

    /**
     * 특정 유저의 점수를 기준으로 "내 위에 몇 명이 있는지" 순위를 계산
     *
     * - targetScore보다 점수가 높은 유저 수를 계산하여,
     *   "내 순위 = (해당 수 + 1)"로 환산 가능
     *
     * @param targetScore 조회 대상 유저의 베이즈 평균 점수
     * @param m 전체 유저 평균 승률
     * @param c 전체 유저 평균 직관 횟수
     * @param year 기준 연도
     * @param teamFilter 팀 필터
     * @return 나보다 점수가 높은 유저 수 (즉, 내 순위 - 1)
     */
    public int calculateMyRankingOrder(final double targetScore, final double m, final double c, final int year,
                                       final TeamFilter teamFilter) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        NumberExpression<Long> w = calculateWinCounts(checkIn, game);
        NumberExpression<Long> n = calculateTotalCountsWithoutDraws(game);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, n);

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

    private NumberExpression<Double> getSafeWinPercent(final NumberExpression<Long> total,
                                                       final NumberExpression<Double> calculatePercent) {
        return new CaseBuilder().when(total.gt(0)).then(calculatePercent).otherwise(0.0);
    }

    private BooleanExpression isMemberNotDeleted(final QMember member) {
        return member.deletedAt.isNull();
    }

    private NumberExpression<Double> calculateWinRankingScore(final double m, final double c,
                                                              final NumberExpression<Long> wins,
                                                              final NumberExpression<Long> total) {
        NumberExpression<Double> denominator = total.doubleValue().add(Expressions.constant(c));

        return new CaseBuilder().when(denominator.ne(0.0))
                .then(wins.doubleValue().add(Expressions.constant(c * m)).divide(denominator))
                .otherwise(0.0);
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
                .where(
                        isFinished(game),
                        isBetweenYear(game, year),
                        isFavoriteTeam(checkIn, member),
                        isMemberNotDeleted(member)
                ).fetchOne();
    }

    private NumberExpression<Long> calculateWinCounts(final QCheckIn checkIn, final QGame game) {
        return new CaseBuilder()
                .when(checkIn.team.eq(game.homeTeam).and(game.homeScore.gt(game.awayScore)))
                .then(1L)
                .when(checkIn.team.eq(game.awayTeam).and(game.awayScore.gt(game.homeScore)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private NumberExpression<Long> calculateTotalCountsWithoutDraws(final QGame game) {
        return new CaseBuilder()
                .when(game.gameState.eq(GameState.COMPLETED).and(game.homeScore.ne(game.awayScore)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private Long calculatePerCheckInCount(final int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;
        return jpaQueryFactory.select(member.countDistinct())
                .from(checkIn)
                .join(game).on(checkIn.game.eq(game))
                .join(member).on(checkIn.member.eq(member))
                .where(
                        isFinished(game),
                        isBetweenYear(game, year),
                        isFavoriteTeam(checkIn, member),
                        isMemberNotDeleted(member)
                )
                .fetchOne();
    }

    private BooleanExpression isFavoriteTeam(final QCheckIn checkIn, final QMember member) {
        return checkIn.team.eq(member.team);
    }
}
