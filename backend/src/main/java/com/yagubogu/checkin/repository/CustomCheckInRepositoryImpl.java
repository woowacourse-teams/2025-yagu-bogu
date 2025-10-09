package com.yagubogu.checkin.repository;

import com.querydsl.core.Tuple;
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
import com.yagubogu.checkin.dto.StatCounts;
import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.game.domain.QScoreBoard;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.stadium.domain.QStadium;
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

    private static final QCheckIn CHECK_IN = QCheckIn.checkIn;
    private static final QGame GAME = QGame.game;
    private static final QMember MEMBER = QMember.member;
    private static final QStadium STADIUM = QStadium.stadium;

    @Override
    public StatCounts findStatCounts(final Member member, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        NumberExpression<Integer> winExpr = new CaseBuilder().when(winCondition(CHECK_IN, GAME)).then(1).otherwise(0);
        NumberExpression<Integer> drawExpr = new CaseBuilder().when(drawCondition(CHECK_IN, GAME)).then(1).otherwise(0);
        NumberExpression<Integer> loseExpr = new CaseBuilder().when(loseCondition(CHECK_IN, GAME)).then(1).otherwise(0);

        return jpaQueryFactory.select(
                        Projections.constructor(
                                StatCounts.class,
                                winExpr.sum(),
                                drawExpr.sum(),
                                loseExpr.sum()
                        )).from(CHECK_IN)
                .join(CHECK_IN.game, CustomCheckInRepositoryImpl.GAME).on(isComplete())
                .where(
                        CHECK_IN.member.eq(member),
                        GAME.date.between(start, end)
                ).fetchOne();
    }

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
    public List<VictoryFairyCountResult> findCheckInAndWinCountBatch(final List<Long> memberIds, final int year) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                VictoryFairyCountResult.class,
                                CHECK_IN.member.id,
                                new CaseBuilder()
                                        .when(drawCondition(CHECK_IN, GAME).not())
                                        .then(1)
                                        .otherwise(0)
                                        .sum()
                                        .intValue(),
                                new CaseBuilder()
                                        .when(winCondition(CHECK_IN, GAME))
                                        .then(1)
                                        .otherwise(0)
                                        .sum()
                                        .intValue()
                        )
                )
                .from(CHECK_IN)
                .join(CHECK_IN.game, GAME)
                .where(
                        CHECK_IN.member.id.in(memberIds),
                        isBetweenYear(year),
                        isComplete()
                )
                .groupBy(CHECK_IN.member.id)
                .fetch();
    }

    @Override
    public int findRecentGamesWinCounts(final Member member, final int year, final int limit) {
        return conditionCountOnRecentGames(member, year, winCondition(QCheckIn.checkIn, QGame.game), limit);
    }

    @Override
    public List<Long> findWinMemberIdByGameId(final long gameId) {
        return jpaQueryFactory.select(CHECK_IN.member.id)
                .from(CHECK_IN)
                .join(GAME).on(CHECK_IN.game.eq(GAME).and(GAME.id.eq(gameId)))
                .where(winCondition(CHECK_IN, GAME))
                .fetch();
    }

    @Override
    public List<Long> findLoseMemberIdByGameId(final long gameId) {
        return jpaQueryFactory.select(CHECK_IN.member.id)
                .from(CHECK_IN)
                .join(GAME).on(CHECK_IN.game.eq(GAME).and(GAME.id.eq(gameId)))
                .where(loseCondition(CHECK_IN, GAME))
                .fetch();
    }

    @Override
    public int findRecentGamesLoseCounts(final Member member, final int year, final int limit) {
        return conditionCountOnRecentGames(member, year, loseCondition(QCheckIn.checkIn, QGame.game), limit);
    }

    /**
     * m : 전체 유저 평균 승률
     * <p>
     * 정의: 특정 연도의 모든 "완료된 경기" 기준으로, 각 유저의 인증(CheckIn) 중에서 승리한 횟수 / 전체 인증 횟수
     * <p>
     * 계산식: - 분자: 승리한 인증 수 (유저가 응원한 팀이 이긴 경우) - 분모: 전체 인증 수 - 전체 유저를 합산하여 평균 승률을 반환
     *
     * @param year 기준 연도
     * @return 전체 유저 평균 승률 (0.0 ~ 1.0). 인증이 없으면 0.0
     */
    @Override
    public double calculateTotalAverageWinRate(final int year) {
        NumberExpression<Long> w = calculateWinCounts(year);
        NumberExpression<Long> n = new CaseBuilder()
                .when(GAME.gameState.eq(GameState.COMPLETED).and(GAME.homeScore.ne(GAME.awayScore)))
                .then(1L).otherwise(0L)
                .sum();

        Tuple tuple = jpaQueryFactory.select(w, n)
                .from(MEMBER)
                .leftJoin(CHECK_IN).on(CHECK_IN.member.eq(MEMBER), isFavoriteTeam())
                .leftJoin(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year))
                .where(isMemberNotDeleted()).fetchOne();

        long winCounts =
                (tuple == null || tuple.get(0, Long.class) == null) ? 0L : tuple.get(0, Long.class).longValue();
        long totalCounts = (tuple == null || tuple.get(1, Long.class) == null) ? 0L : tuple.get(1, Long.class);

        return (totalCounts == 0) ? 0.0 : (double) winCounts / totalCounts;
    }

    /**
     * c : 전체 유저 평균 직관 횟수
     * <p>
     * 정의: 특정 연도의 총 직관(체크인) 횟수 / 직관에 참여한 유저 수
     * <p>
     * 계산식: - 분자: 해당 연도의 모든 인증 기록 수 (ΣN) - 분모: 해당 연도에 한 번이라도 직관한 유저 수
     *
     * @param year 기준 연도
     * @return 전체 유저 평균 직관 횟수 (0.0 이상)
     */
    @Override
    public double calculateAverageCheckInCount(final int year) {
        Long totalCheckInCount = calculateTotalCheckInCount(year);
        Long perCheckInCount = calculatePerCheckInCount(year);

        return (perCheckInCount == 0) ? 0.0 : (double) totalCheckInCount / perCheckInCount;
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


    @Override
    public int findRecentGamesDrawCounts(final Member member, final int year, final int limit) {
        return conditionCountOnRecentGames(member, year, drawCondition(QCheckIn.checkIn, QGame.game), limit);
    }

    // 내 직관 내역 조회
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

        BooleanExpression myTeamWinFilter = getMyTeamWinFilter(resultFilter, CHECK_IN);
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
                        isCompleteOrCanceled(),
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
                        isCompleteOrCanceled(),
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

    private BooleanExpression isMemberNotDeleted() {
        return MEMBER.deletedAt.isNull();
    }

    private BooleanExpression isComplete() {
        return GAME.gameState.eq(GameState.COMPLETED);
    }

    private BooleanExpression isCompleteOrCanceled() {
        return GAME.gameState.eq(GameState.COMPLETED).or(GAME.gameState.eq(GameState.CANCELED));
    }

    private BooleanExpression isBetweenYear(final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return GAME.date.between(start, end);
    }

    private Long calculateTotalCheckInCount(final int year) {
        return jpaQueryFactory.select(CHECK_IN.count())
                .from(CHECK_IN)
                .join(GAME)
                .on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year), GAME.homeScore.ne(GAME.awayScore))
                .join(MEMBER).on(CHECK_IN.member.eq(MEMBER), isFavoriteTeam(), isMemberNotDeleted())
                .fetchOne();
    }

    private NumberExpression<Long> calculateWinCounts(final int year) {
        return new CaseBuilder()
                .when(CHECK_IN.team.eq(GAME.homeTeam).and(GAME.homeScore.gt(GAME.awayScore))
                        .and(isBetweenYear(year)))
                .then(1L)
                .when(CHECK_IN.team.eq(GAME.awayTeam).and(GAME.awayScore.gt(GAME.homeScore))
                        .and(isBetweenYear(year)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private Long calculatePerCheckInCount(final int year) {
        return jpaQueryFactory.select(MEMBER.countDistinct())
                .from(CHECK_IN)
                .join(GAME)
                .on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year), GAME.homeScore.ne(GAME.awayScore))
                .join(MEMBER).on(CHECK_IN.member.eq(MEMBER), isFavoriteTeam(), isMemberNotDeleted())
                .fetchOne();
    }

    private BooleanExpression isFavoriteTeam() {
        return CHECK_IN.team.eq(MEMBER.team);
    }

    private int conditionCountOnRecentGames(
            final Member member,
            final int year,
            final BooleanExpression condition,
            final int limit
    ) {
        QCheckIn qCheckIn = QCheckIn.checkIn;
        QGame qGame = QGame.game;

        List<Long> recentGameIds = findRecentGameIdsByYear(member, year, limit);
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

    private List<Long> findRecentGameIdsByYear(final Member member, final int year, final int limit) {
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

    private OrderSpecifier<LocalDate> getOrderByFilter(final CheckInOrderFilter orderFilter, QGame game) {
        if (orderFilter == CheckInOrderFilter.OLDEST) {
            return game.date.asc();
        }
        return game.date.desc();
    }

    private BooleanExpression getMyTeamWinFilter(CheckInResultFilter resultFilter, QCheckIn checkIn) {
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

    private static BooleanExpression isMyCurrentFavorite(final Member member, final QCheckIn checkIn) {
        return checkIn.team.eq(member.getTeam());
    }

    private BooleanExpression isBetweenYear(QGame game, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        return game.date.between(start, end);
    }
}
