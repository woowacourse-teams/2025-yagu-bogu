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
import com.yagubogu.game.domain.QScoreBoard;
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

    public static final QCheckIn CHECK_IN = QCheckIn.checkIn;
    public static final QGame GAME = QGame.game;
    public static final QStadium STADIUM = QStadium.stadium;

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

    // 구장 방문 총 횟수
    // 행운의 구장 계산시 사용
    // 구장별 승률 => 해당 구장에서 승리한 횟수 / (해당 구장에서 승리한 횟수 + 해당 구장에서 패배한 횟수)
    // 경기 완료 여부 o, 무승부 x, 내 응원팀 여부 o
    public int countTotalFavoriteTeamGamesByStadiumAndMember(Stadium stadium, Member member, int year) {
        return jpaQueryFactory.select(CHECK_IN.count())
                .from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .where(
                        CHECK_IN.member.eq(member),
                        isBetweenYear(GAME, year),
                        GAME.stadium.eq(stadium),
                        isMyCurrentFavorite(member, CHECK_IN),
                        drawCondition(CHECK_IN, GAME).not(),
                        GAME.gameState.eq(GameState.COMPLETED)
                )
                .fetchOne()
                .intValue();
    }

    public int countWinsFavoriteTeamByStadiumAndMember(Stadium stadium, Member member, int year) {
        return jpaQueryFactory.select(CHECK_IN.count())
                .from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .where(
                        CHECK_IN.member.eq(member),
                        isBetweenYear(CHECK_IN.game, year),
                        isMyCurrentFavorite(member, CHECK_IN),
                        winCondition(CHECK_IN, CHECK_IN.game),
                        GAME.stadium.eq(stadium)
                ).fetchOne()
                .intValue();
    }

    // 경기 완료 여부 관계 x, 내 응원 팀 여부 관계 x
    // 내 인증 횟수 반환
    public int countByMemberAndYear(Member member, int year) {
        return jpaQueryFactory.select(CHECK_IN.count())
                .from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .where(
                        CHECK_IN.member.eq(member),
                        isBetweenYear(GAME, year)
                ).fetchOne()
                .intValue();
    }

    // 내 응원 팀 여부 관계 o, 게임 완료 여부 관계 x(취소된 경기도 보여줌)
    public List<CheckInGameResponse> findCheckInHistory(
            Member member,
            Team team,
            int year,
            final CheckInResultFilter resultFilter,
            final CheckInOrderFilter orderFilter
    ) {
        QTeam home = new QTeam("home");
        QTeam away = new QTeam("away");
        QScoreBoard homeScoreBoard = new QScoreBoard("homeScoreBoard");
        QScoreBoard awayScoreBoard = new QScoreBoard("awayScoreBoard");

        BooleanExpression myTeamWinFilter = getMyTeamWinFilter(resultFilter, member, CHECK_IN);
        OrderSpecifier<LocalDate> order = getOrderByFilter(orderFilter, GAME);
        return jpaQueryFactory.select(Projections.constructor(
                        CheckInGameResponse.class,
                        CHECK_IN.id,
                        STADIUM.fullName,
                        homeTeamResp(GAME, home, team),
                        awayTeamResp(GAME, away, team),
                        GAME.date,
                        GAME.homeScoreBoard,
                        GAME.awayScoreBoard
                )).from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .join(GAME.stadium, STADIUM)
                .join(GAME.homeTeam, home)
                .join(GAME.awayTeam, away)
                .leftJoin(GAME.homeScoreBoard, homeScoreBoard)
                .leftJoin(GAME.awayScoreBoard, awayScoreBoard)
                .where(
                        CHECK_IN.member.eq(member),
                        isBetweenYear(GAME, year),
                        isMyCurrentFavorite(member, CHECK_IN),
                        myTeamWinFilter
                ).orderBy(order)
                .fetch();
    }

    public List<GameWithFanCountsResponse> findGamesWithFanCountsByDate(LocalDate date) {
        QCheckIn CHECK_IN = QCheckIn.checkIn;
        QTeam home = new QTeam("home");
        QTeam away = new QTeam("away");
        NumberExpression<Long> homeFans = new CaseBuilder()
                .when(CHECK_IN.team.eq(GAME.homeTeam)).then(1L)
                .otherwise(0L)
                .sum();
        NumberExpression<Long> awayFans = new CaseBuilder()
                .when(CHECK_IN.team.eq(GAME.awayTeam)).then(1L)
                .otherwise(0L)
                .sum();

        return jpaQueryFactory.select(
                        Projections.constructor(
                                GameWithFanCountsResponse.class,
                                GAME,
                                CHECK_IN.id.count(),
                                homeFans,
                                awayFans
                        )
                ).from(GAME)
                .join(GAME.homeTeam, home).fetchJoin()
                .join(GAME.awayTeam, away).fetchJoin()
                .leftJoin(CHECK_IN).on(CHECK_IN.game.eq(GAME))
                .where(GAME.date.eq(date))
                .groupBy(GAME.id)
                .fetch();
    }

    // 현재 내가 응원하는 팀만 통계에 집계한다
    // 내가 응원하는 팀 o, 경기 완료된 것 o(경기 완료된 것만 통계에 집계)
    public AverageStatistic findAverageStatistic(Member member) {
        NumberExpression<Integer> myRuns = new CaseBuilder()
                .when(GAME.homeTeam.eq(CHECK_IN.team)).then(GAME.homeScoreBoard.runs)
                .when(GAME.awayTeam.eq(CHECK_IN.team)).then(GAME.awayScoreBoard.runs)
                .otherwise((Integer) null);

        NumberExpression<Integer> myErrors = new CaseBuilder()
                .when(GAME.homeTeam.eq(CHECK_IN.team)).then(GAME.homeScoreBoard.errors)
                .when(GAME.awayTeam.eq(CHECK_IN.team)).then(GAME.awayScoreBoard.errors)
                .otherwise((Integer) null);

        NumberExpression<Integer> myHits = new CaseBuilder()
                .when(GAME.homeTeam.eq(CHECK_IN.team)).then(GAME.homeScoreBoard.hits)
                .when(GAME.awayTeam.eq(CHECK_IN.team)).then(GAME.awayScoreBoard.hits)
                .otherwise((Integer) null);

        NumberExpression<Integer> opponentRuns = new CaseBuilder()
                .when(GAME.homeTeam.eq(CHECK_IN.team)).then(GAME.awayScoreBoard.runs)
                .when(GAME.awayTeam.eq(CHECK_IN.team)).then(GAME.homeScoreBoard.runs)
                .otherwise((Integer) null);

        NumberExpression<Integer> opponentHits = new CaseBuilder()
                .when(GAME.homeTeam.eq(CHECK_IN.team)).then(GAME.awayScoreBoard.hits)
                .when(GAME.awayTeam.eq(CHECK_IN.team)).then(GAME.homeScoreBoard.hits)
                .otherwise((Integer) null);

        return jpaQueryFactory
                .select(Projections.constructor(
                        AverageStatistic.class,
                        myRuns.avg(),
                        opponentRuns.avg(),
                        myErrors.avg(),
                        myHits.avg(),
                        opponentHits.avg()
                )).from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .where(
                        CHECK_IN.member.eq(member),
                        GAME.homeTeam.eq(CHECK_IN.team).or(GAME.awayTeam.eq(CHECK_IN.team)),
                        isMyCurrentFavorite(member, CHECK_IN),
                        GAME.gameState.eq(GameState.COMPLETED)
                ).fetchOne();
    }

    // 구장별 인증 횟수
    // 내가 응원하는 팀 o, 완료된 경기만 x
    public List<StadiumCheckInCountResponse> findStadiumCheckInCounts(
            Member member,
            int year
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                StadiumCheckInCountResponse.class,
                                STADIUM.id,
                                STADIUM.location,
                                CHECK_IN.id.count()
                        )).from(STADIUM)
                .leftJoin(CHECK_IN).on(
                        CHECK_IN.game.stadium.eq(STADIUM)
                                .and(CHECK_IN.member.eq(member))
                                .and(isBetweenYear(CHECK_IN.game, year))
                ).groupBy(STADIUM.id, STADIUM.location)
                .fetch();
    }

    public List<OpponentWinRateRow> findOpponentWinRates(
            Member member,
            Team team,
            int year
    ) {
        QTeam opponentTeam = QTeam.team;

        BooleanExpression myHome = GAME.homeTeam.eq(team);
        BooleanExpression myAway = GAME.awayTeam.eq(team);

        BooleanExpression hasCheckIn = CHECK_IN.id.isNotNull();

        NumberExpression<Integer> winExpr =
                new CaseBuilder()
                        .when(hasCheckIn.and(myHome).and(GAME.homeScore.gt(GAME.awayScore))).then(1)
                        .when(hasCheckIn.and(myAway).and(GAME.awayScore.gt(GAME.homeScore))).then(1)
                        .otherwise(0);

        NumberExpression<Integer> loseExpr =
                new CaseBuilder()
                        .when(hasCheckIn.and(myHome).and(GAME.homeScore.lt(GAME.awayScore))).then(1)
                        .when(hasCheckIn.and(myAway).and(GAME.awayScore.lt(GAME.homeScore))).then(1)
                        .otherwise(0);

        NumberExpression<Integer> drawExpr =
                new CaseBuilder()
                        .when(hasCheckIn.and(GAME.homeScore.eq(GAME.awayScore)))
                        .then(1)
                        .otherwise(0);

        BooleanExpression gameOnOpponent =
                GAME.gameState.eq(GameState.COMPLETED)
                        .and(isBetweenYear(GAME, year))
                        .and(
                                myHome.and(GAME.awayTeam.id.eq(opponentTeam.id)
                                ).or(myAway.and(GAME.homeTeam.id.eq(opponentTeam.id)))
                        );

        BooleanExpression checkInFilter = CHECK_IN.member.eq(member).and(CHECK_IN.team.eq(team));

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
                .leftJoin(GAME).on(gameOnOpponent)
                .leftJoin(CHECK_IN).on(CHECK_IN.game.eq(GAME).and(checkInFilter))
                .where(opponentTeam.ne(team))
                .groupBy(opponentTeam.id, opponentTeam.name, opponentTeam.shortName, opponentTeam.teamCode)
                .fetch();
    }

    private OrderSpecifier<LocalDate> getOrderByFilter(final CheckInOrderFilter orderFilter, QGame game) {
        if (orderFilter == CheckInOrderFilter.OLDEST) {
            return game.date.asc();
        }
        return game.date.desc();
    }

    private BooleanExpression getMyTeamWinFilter(CheckInResultFilter resultFilter, Member member, QCheckIn checkIn) {
        if (resultFilter == CheckInResultFilter.ALL) {
            return null;
        }
        return winCondition(checkIn, checkIn.game);
    }


    private ConstructorExpression<CheckInGameTeamResponse> homeTeamResp(QGame g, final QTeam home, Team team) {
        return Projections.constructor(
                CheckInGameTeamResponse.class,
                home.teamCode,
                home.shortName,
                g.homeScore,
                new CaseBuilder().when(home.eq(team)).then(true).otherwise(false),
                g.homePitcher
        );
    }

    private ConstructorExpression<CheckInGameTeamResponse> awayTeamResp(QGame g, final QTeam away, Team team) {
        return Projections.constructor(
                CheckInGameTeamResponse.class,
                away.teamCode,
                away.shortName,
                g.awayScore,
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
