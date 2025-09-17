package com.yagubogu.checkin.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.member.domain.Member;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomCheckInRepositoryImpl implements CustomCheckInRepository {

    private static final int RECENT_LIMIT = 10;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public int findWinCounts(final Member member, final int year) {
        return conditionCount(member, year, winCondition(QCheckIn.checkIn, QGame.game));
    }

    @Override
    public int findLoseCounts(final Member member, final int year) {
        return conditionCount(member, year, loseCondition(QCheckIn.checkIn, QGame.game));
    }

    @Override
    public int findDrawCounts(final Member member, final int year) {
        return conditionCount(member, year, drawCondition(QCheckIn.checkIn, QGame.game));
    }

    @Override
    public int findRecentTenGamesWinCounts(final Member member, final int year) {
        return conditionCountOnRecentGames(member, year, winCondition(QCheckIn.checkIn, QGame.game));
    }

    @Override
    public int findRecentTenGamesLoseCounts(final Member member, final int year) {
        return conditionCountOnRecentGames(member, year, loseCondition(QCheckIn.checkIn, QGame.game));
    }

    @Override
    public int findRecentTenGamesDrawCounts(final Member member, final int year) {
        return conditionCountOnRecentGames(member, year, drawCondition(QCheckIn.checkIn, QGame.game));
    }

    private int conditionCount(final Member member, final int year, final BooleanExpression condition) {
        QCheckIn qCheckIn = QCheckIn.checkIn;
        QGame qGame = QGame.game;

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        Long result = jpaQueryFactory
                .select(qCheckIn.id.count())
                .from(qCheckIn)
                .join(qCheckIn.game, qGame)
                .where(
                        qCheckIn.member.eq(member),
                        qGame.date.between(start, end),
                        qGame.gameState.eq(GameState.COMPLETED),
                        condition
                )
                .fetchOne();

        return result.intValue();
    }

    private int conditionCountOnRecentGames(
            final Member member,
            final int year,
            final BooleanExpression condition
    ) {
        QCheckIn qCheckIn = QCheckIn.checkIn;
        QGame qGame = QGame.game;

        List<Long> recentGameIds = findRecentGameIds(member, year, RECENT_LIMIT);
        if (recentGameIds.isEmpty()) {
            return 0;
        }

        Long result = jpaQueryFactory
                .select(qCheckIn.count())
                .from(qCheckIn)
                .join(qCheckIn.game, qGame)
                .where(
                        qCheckIn.member.eq(member),
                        qGame.id.in(recentGameIds),
                        condition
                )
                .fetchOne();

        return result == null ? 0 : result.intValue();
    }

    private List<Long> findRecentGameIds(final Member member, final int year, final int limit) {
        QCheckIn qCheckIn = QCheckIn.checkIn;
        QGame qGame = QGame.game;

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return jpaQueryFactory
                .select(qGame.id)
                .from(qCheckIn)
                .join(qCheckIn.game, qGame)
                .where(
                        qCheckIn.member.eq(member),
                        qGame.date.between(start, end),
                        qGame.gameState.eq(GameState.COMPLETED)
                )
                .orderBy(qCheckIn.id.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression winCondition(final QCheckIn checkIn, final QGame game) {
        BooleanExpression awayWin = checkIn.team.eq(game.awayTeam).and(game.awayScore.gt(game.homeScore));
        BooleanExpression homeWin = checkIn.team.eq(game.homeTeam).and(game.homeScore.gt(game.awayScore));

        return awayWin.or(homeWin);
    }

    private BooleanExpression loseCondition(final QCheckIn checkIn, final QGame game) {
        BooleanExpression awayLose = checkIn.team.eq(game.awayTeam).and(game.awayScore.lt(game.homeScore));
        BooleanExpression homeLose = checkIn.team.eq(game.homeTeam).and(game.homeScore.lt(game.awayScore));

        return awayLose.or(homeLose);
    }

    private BooleanExpression drawCondition(final QCheckIn checkIn, final QGame game) {
        BooleanExpression awayDraw = checkIn.team.eq(game.awayTeam).and(game.awayScore.eq(game.homeScore));
        BooleanExpression homeDraw = checkIn.team.eq(game.homeTeam).and(game.homeScore.eq(game.awayScore));

        return awayDraw.or(homeDraw);
    }
}
