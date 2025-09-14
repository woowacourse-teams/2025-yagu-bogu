package com.yagubogu.checkin.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.member.domain.QMember;
import java.time.LocalDate;
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

        BooleanExpression finished = isFinished(game);

        NumberExpression<Long> isWin =
                new CaseBuilder()
                        .when(
                                checkIn.team.eq(game.homeTeam)
                                        .and(game.homeScore.gt(game.awayScore))
                        )
                        .then(1L)
                        .when(
                                checkIn.team.eq(game.awayTeam)
                                        .and(game.awayScore.gt(game.homeScore))
                        )
                        .then(1L)
                        .otherwise(0L);

        Tuple tuple = jpaQueryFactory.select(isWin.sum(), checkIn.count())
                .from(checkIn)
                .join(game).on(checkIn.game.eq(game))
                .join(member).on(checkIn.member.eq(member))
                .where(
                        finished,
                        isBetweenYear(game, year),
                        isFavoriteTeam(checkIn, member)
                )
                .fetchOne();

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

    private BooleanExpression isFinished(final QGame game) {
        return game.gameState.eq(GameState.COMPLETED);
    }

    private BooleanExpression isBetweenYear(QGame game, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return game.date.between(start, end);
    }

    private Long calculateTotalCheckInCount(int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QMember member = QMember.member;

        return jpaQueryFactory.select(checkIn.count())
                .from(checkIn)
                .join(game).on(checkIn.game.eq(game))
                .join(member).on(checkIn.member.eq(member))
                .where(
                        isFinished(game),
                        isBetweenYear(game, year),
                        isFavoriteTeam(checkIn, member)
                )
                .fetchOne();
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
                        isFavoriteTeam(checkIn, member)
                ).fetchOne();
    }

    private BooleanExpression isFavoriteTeam(final QCheckIn checkIn, final QMember member) {
        return checkIn.team.eq(member.team);
    }
}
