package com.yagubogu.checkin.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInGameTeamResponse;
import com.yagubogu.checkin.dto.GameWithFanCountsResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountResponse;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.QStadium;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.OpponentWinRateRow;
import com.yagubogu.team.domain.QTeam;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomCheckInRepositoryImpl implements CustomCheckInRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public int findWinCounts(Member member, final int year) {
        return conditionCount(member, year, winCondition(QCheckIn.checkIn, QGame.game));
    }

    public int findLoseCounts(Member member, final int year) {
        return conditionCount(member, year, loseCondition(QCheckIn.checkIn, QGame.game));
    }

    public int findDrawCounts(Member member, final int year) {
        return conditionCount(member, year, drawCondition(QCheckIn.checkIn, QGame.game));
    }

    public int countTotalFavoriteTeamGamesByStadiumAndMember(Stadium stadium, Member member, int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;

        return jpaQueryFactory.select(checkIn.count())
                .from(checkIn)
                .join(checkIn.game, game)
                .where(
                        checkIn.member.eq(member),
                        isBetweenYear(game, year),
                        game.stadium.eq(stadium),
                        isMyCurrentFavorite(member, checkIn)
                )
                .fetchOne()
                .intValue();

    }

    public int countWinsFavoriteTeamByStadiumAndMember(Stadium stadium, Member member, int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;

        return jpaQueryFactory.select(checkIn.count())
                .from(checkIn)
                .join(checkIn.game, game)
                .where(
                        checkIn.member.eq(member),
                        isBetweenYear(checkIn.game, year),
                        isMyCurrentFavorite(member, checkIn),
                        winCondition(checkIn, checkIn.game),
                        game.stadium.eq(stadium)
                ).fetchOne()
                .intValue();
    }

    // 경기 완료 여부 관계 x, 내 응원 팀 여부 관계 x
    // 내 인증 횟수 반환
    public int countByMemberAndYear(Member member, int year) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;

        return jpaQueryFactory.select(checkIn.count())
                .from(checkIn)
                .join(checkIn.game, game)
                .where(
                        checkIn.member.eq(member),
                        isBetweenYear(game, year),
                        isMyCurrentFavorite(member, checkIn)
                ).fetchOne()
                .intValue();
    }

    // 내 응원 팀 여부 관계 x(내가 인증한 경기는 모두 보여줌), 게임 완료 여부 관계 x(취소된 경기도 보여줌)
    public List<CheckInGameResponse> findCheckInHistory(
            Member member,
            Team team,
            int year,
            final CheckInResultFilter resultFilter,
            final CheckInOrderFilter orderFilter
    ) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QStadium stadium = QStadium.stadium;
        QTeam home = new QTeam("home");
        QTeam away = new QTeam("away");

        BooleanExpression myTeamWin = getMyTeamWin(resultFilter, member, checkIn);
        OrderSpecifier<LocalDate> order = getOrderByFilter(orderFilter, game);
        return jpaQueryFactory.select(Projections.constructor(
                        CheckInGameResponse.class,
                        checkIn.id,
                        stadium.fullName,
                        homeTeamResp(game, home, team),
                        awayTeamResp(game, away, team),
                        game.date,
                        game.homeScoreBoard,
                        game.awayScoreBoard
                )).from(checkIn)
                .join(checkIn.game, game)
                .join(game.stadium, stadium)
                .join(game.homeTeam, home)
                .join(game.awayTeam, away)
                .where(
                        checkIn.member.eq(member),
                        isBetweenYear(game, year),
                        myTeamWin
                ).orderBy(order)
                .fetch();
    }

    public List<GameWithFanCountsResponse> findGamesWithFanCountsByDate(LocalDate date) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;
        QTeam home = new QTeam("home");
        QTeam away = new QTeam("away");
        NumberExpression<Long> homeFans = new CaseBuilder()
                .when(checkIn.team.eq(game.homeTeam)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> awayFans = new CaseBuilder()
                .when(checkIn.team.eq(game.awayTeam)).then(1L)
                .otherwise(0L)
                .sum();

        return jpaQueryFactory.select(
                        Projections.constructor(
                                GameWithFanCountsResponse.class,
                                game,
                                checkIn.id.count(),
                                homeFans,
                                awayFans
                        )
                ).from(game)
                .join(game.homeTeam, home).fetchJoin()
                .join(game.awayTeam, away).fetchJoin()
                .leftJoin(checkIn).on(checkIn.game.eq(game))
                .where(game.date.eq(date))
                .groupBy(game.id)
                .fetch();
    }

    // 현재 내가 응원하는 팀만 통계에 집계한다
    // 내가 응원하는 팀 o, 경기 완료된 것 o(경기 완료된 것만 통계에 집계)
    public AverageStatistic findAverageStatistic(Member member) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;

        NumberExpression<Integer> myRuns = new CaseBuilder()
                .when(game.homeTeam.eq(checkIn.team)).then(game.homeScoreBoard.runs)
                .when(game.awayTeam.eq(checkIn.team)).then(game.awayScoreBoard.runs)
                .otherwise((Integer) null);

        NumberExpression<Integer> myErrors = new CaseBuilder()
                .when(game.homeTeam.eq(checkIn.team)).then(game.homeScoreBoard.errors)
                .when(game.awayTeam.eq(checkIn.team)).then(game.awayScoreBoard.errors)
                .otherwise((Integer) null);

        NumberExpression<Integer> myHits = new CaseBuilder()
                .when(game.homeTeam.eq(checkIn.team)).then(game.homeScoreBoard.hits)
                .when(game.awayTeam.eq(checkIn.team)).then(game.awayScoreBoard.hits)
                .otherwise((Integer) null);

        NumberExpression<Integer> opponentRuns = new CaseBuilder()
                .when(game.homeTeam.eq(checkIn.team)).then(game.awayScoreBoard.runs)
                .when(game.awayTeam.eq(checkIn.team)).then(game.homeScoreBoard.runs)
                .otherwise((Integer) null);

        NumberExpression<Integer> opponentHits = new CaseBuilder()
                .when(game.homeTeam.eq(checkIn.team)).then(game.awayScoreBoard.hits)
                .when(game.awayTeam.eq(checkIn.team)).then(game.homeScoreBoard.hits)
                .otherwise((Integer) null);

        return jpaQueryFactory
                .select(Projections.constructor(
                        AverageStatistic.class,
                        myRuns.avg(),
                        opponentRuns.avg(),
                        myErrors.avg(),
                        myHits.avg(),
                        opponentHits.avg()
                )).from(checkIn)
                .join(checkIn.game, game)
                .where(
                        checkIn.member.eq(member),
                        game.homeTeam.eq(checkIn.team).or(game.awayTeam.eq(checkIn.team)),
                        isMyCurrentFavorite(member, checkIn),
                        game.gameState.eq(GameState.COMPLETED)
                ).fetchOne();
    }

    // 구장별 인증 횟수?
    // 내가 응원하는 팀 o, 완료된 경기만 x
    public List<StadiumCheckInCountResponse> findStadiumCheckInCounts(
            Member member,
            int year
    ) {
        QStadium stadium = QStadium.stadium;
        QCheckIn checkIn = QCheckIn.checkIn;

        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                StadiumCheckInCountResponse.class,
                                stadium.id,
                                stadium.location,
                                checkIn.id.count()
                        )).from(stadium)
                .leftJoin(checkIn).on(
                        checkIn.game.stadium.eq(stadium)
                                .and(checkIn.member.eq(member))
                                .and(isBetweenYear(checkIn.game, year))
                ).groupBy(stadium.id, stadium.location)
                .fetch();
    }

    public List<OpponentWinRateRow> findOpponentWinRates(
            Member member,
            Team team,
            int year
    ) {
        QTeam opponentTeam = QTeam.team;
        QGame game = QGame.game;
        QCheckIn checkIn = QCheckIn.checkIn;

        BooleanExpression myHome = game.homeTeam.eq(team);
        BooleanExpression myAway = game.awayTeam.eq(team);

        BooleanExpression hasCheckIn = checkIn.id.isNotNull();

        NumberExpression<Integer> winExpr =
                new CaseBuilder()
                        .when(hasCheckIn.and(myHome).and(game.homeScore.gt(game.awayScore))).then(1)
                        .when(hasCheckIn.and(myAway).and(game.awayScore.gt(game.homeScore))).then(1)
                        .otherwise(0);

        NumberExpression<Integer> loseExpr =
                new CaseBuilder()
                        .when(hasCheckIn.and(myHome).and(game.homeScore.lt(game.awayScore))).then(1)
                        .when(hasCheckIn.and(myAway).and(game.awayScore.lt(game.homeScore))).then(1)
                        .otherwise(0);

        NumberExpression<Integer> drawExpr =
                new CaseBuilder()
                        .when(hasCheckIn
                                .and(game.homeScore.eq(game.awayScore))
                        ).then(1)
                        .otherwise(0);

        BooleanExpression gameOnOpponent =
                game.gameState.eq(GameState.COMPLETED)
                        .and(isBetweenYear(game, year))
                        .and(
                                myHome.and(game.awayTeam.id.eq(opponentTeam.id))
                                        .or(myAway.and(game.homeTeam.id.eq(opponentTeam.id)))
                        );

        BooleanExpression checkInFilter =
                checkIn.member.eq(member)
                        .and(checkIn.team.eq(team));

        return jpaQueryFactory
                .select(Projections.constructor(
                        OpponentWinRateRow.class,
                        opponentTeam.id,
                        opponentTeam.name,
                        opponentTeam.shortName,
                        opponentTeam.teamCode,
                        winExpr.sum().coalesce(0),
                        loseExpr.sum().coalesce(0),
                        drawExpr.sum().coalesce(0)
                ))
                .from(opponentTeam)
                .leftJoin(checkIn).on(checkIn.game.eq(game).and(checkInFilter))
                .leftJoin(game).on(gameOnOpponent
                        .and(checkIn.game.eq(game))
                ).where(opponentTeam.ne(team))
                .groupBy(opponentTeam.id, opponentTeam.name, opponentTeam.shortName, opponentTeam.teamCode)
                .fetch();
    }

    private OrderSpecifier<LocalDate> getOrderByFilter(final CheckInOrderFilter orderFilter, QGame game) {
        if (orderFilter == CheckInOrderFilter.OLDEST) {
            return game.date.asc();
        }
        return game.date.desc();
    }

    private BooleanExpression getMyTeamWin(CheckInResultFilter resultFilter, Member member, QCheckIn checkIn) {
        if (resultFilter == CheckInResultFilter.ALL) {
            return null;
        }
        return isMyCurrentFavorite(member, checkIn)
                .and(winCondition(checkIn, checkIn.game));
    }


    private ConstructorExpression<CheckInGameTeamResponse> homeTeamResp(QGame g, final QTeam home, Team team) {
        return Projections.constructor(
                CheckInGameTeamResponse.class,
                home.teamCode,
                home.shortName,
                g.homeScoreBoard.runs,
                new CaseBuilder().when(home.eq(team)).then(true).otherwise(false),
                g.homePitcher
        );
    }

    private ConstructorExpression<CheckInGameTeamResponse> awayTeamResp(QGame g, final QTeam away, Team team) {
        return Projections.constructor(
                CheckInGameTeamResponse.class,
                away.teamCode,
                away.shortName,
                g.awayScoreBoard.runs,
                new CaseBuilder().when(away.eq(team)).then(true).otherwise(false),
                g.awayPitcher
        );
    }

    private int conditionCount(Member member, final int year, BooleanExpression condition) {
        QCheckIn checkIn = QCheckIn.checkIn;
        QGame game = QGame.game;

        return jpaQueryFactory.select(checkIn.count())
                .from(checkIn)
                .join(checkIn.game, game)
                .where(
                        checkIn.member.eq(member),
                        isBetweenYear(game, year),
                        game.gameState.eq(GameState.COMPLETED),
                        isMyCurrentFavorite(member, checkIn), // 현재 내가 응원하는 팀 기준으로 경기 기록을 계산
                        condition
                ).fetchOne()
                .intValue();
    }

    private static BooleanExpression isMyCurrentFavorite(final Member member, final QCheckIn checkIn) {
        return checkIn.team.eq(member.getTeam());
    }

    private BooleanExpression winCondition(QCheckIn checkIn, QGame game) {
        BooleanExpression awayWin = checkIn.team.eq(game.awayTeam).and(game.awayScore.gt(game.homeScore));
        BooleanExpression homeWin = checkIn.team.eq(game.homeTeam).and(game.homeScore.gt(game.awayScore));

        return awayWin.or(homeWin);
    }

    private BooleanExpression loseCondition(QCheckIn checkIn, QGame game) {
        BooleanExpression awayLose = checkIn.team.eq(game.awayTeam).and(game.awayScore.lt(game.homeScore));
        BooleanExpression homeLose = checkIn.team.eq(game.homeTeam).and(game.homeScore.lt(game.awayScore));

        return awayLose.or(homeLose);
    }

    private BooleanExpression drawCondition(QCheckIn checkIn, QGame game) {
        BooleanExpression awayDraw = checkIn.team.eq(game.awayTeam).and(game.awayScore.eq(game.homeScore));
        BooleanExpression homeDraw = checkIn.team.eq(game.homeTeam).and(game.homeScore.eq(game.awayScore));

        return awayDraw.or(homeDraw);
    }

    private BooleanExpression isBetweenYear(QGame game, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return game.date.between(start, end);
    }
}
