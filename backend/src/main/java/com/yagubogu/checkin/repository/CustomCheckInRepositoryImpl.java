package com.yagubogu.checkin.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.checkin.dto.CheckInGameParam;
import com.yagubogu.checkin.dto.CheckInGameTeamParam;
import com.yagubogu.checkin.dto.GameWithFanCountsParam;
import com.yagubogu.checkin.dto.StadiumCheckInCountParam;
import com.yagubogu.checkin.dto.StatCountsParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.game.domain.QScoreBoard;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.QMember;
import com.yagubogu.member.domain.QNickname;
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

    private static final QCheckIn CHECK_IN = QCheckIn.checkIn;
    private static final QGame GAME = QGame.game;
    private static final QMember MEMBER = QMember.member;
    private static final QTeam TEAM = QTeam.team;
    private static final QNickname NICKNAME = QNickname.nickname;
    private static final QStadium STADIUM = QStadium.stadium;

    @Override
    public StatCountsParam findStatCounts(final Member member, final int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        NumberExpression<Integer> winExpr = new CaseBuilder().when(winCondition(CHECK_IN, GAME)).then(1).otherwise(0);
        NumberExpression<Integer> drawExpr = new CaseBuilder().when(drawCondition(CHECK_IN, GAME)).then(1).otherwise(0);
        NumberExpression<Integer> loseExpr = new CaseBuilder().when(loseCondition(CHECK_IN, GAME)).then(1).otherwise(0);

        return jpaQueryFactory.select(
                        Projections.constructor(
                                StatCountsParam.class,
                                winExpr.sum(),
                                drawExpr.sum(),
                                loseExpr.sum()
                        )).from(CHECK_IN)
                .join(CHECK_IN.game, CustomCheckInRepositoryImpl.GAME).on(isComplete())
                .where(
                        CHECK_IN.member.eq(member),
                        GAME.date.between(start, end),
                        isMyCurrentFavorite(member, CHECK_IN)
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
    public int findRecentGamesWinCounts(final Member member, final int year, final int limit) {
        return conditionCountOnRecentGames(member, year, winCondition(QCheckIn.checkIn, QGame.game), limit);
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

    /**
     * 전체 유저 중 "승리 요정 랭킹" 상위 N명을 조회
     * <p>
     * - 베이즈 평균 공식을 기반으로 점수(score)를 계산 score = (W + C * m) / (N + C) W: 개인 승리 횟수 N: 개인 직관 수 m: 전체 평균 승률 C: 전체 평균 직관 횟수
     *
     * @param m          전체 유저 평균 승률
     * @param c          전체 유저 평균 직관 횟수
     * @param year       기준 연도
     * @param teamFilter 팀 필터(ALL 또는 특정 팀만 조회)
     * @param limit      상위 N명 제한
     * @return VictoryFairyRank DTO 리스트 (상위 랭킹)
     */
    @Override
    public List<VictoryFairyRankParam> findTopVictoryRanking(
            final double m,
            final double c,
            final int year,
            final TeamFilter teamFilter,
            final int limit
    ) {
        NumberExpression<Long> w = calculateWinCounts(year);
        NumberExpression<Long> n = calculateTotalCountsWithoutDraws(year);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, n);

        NumberExpression<Double> calculatePercent = w.multiply(100.0).divide(n).doubleValue();
        NumberExpression<Double> safeWinPercent = getSafeWinPercent(n, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(
                                VictoryFairyRankParam.class, score, NICKNAME.value, MEMBER.imageUrl,
                                MEMBER.team.shortName, safeWinPercent))
                .from(MEMBER)
                .join(CHECK_IN).on(CHECK_IN.member.eq(MEMBER), isFavoriteTeam())
                .leftJoin(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year), isMyTeamInGame())
                .leftJoin(TEAM).on(MEMBER.team.eq(TEAM))
                .where(isMemberNotDeleted(), filterByTeam(teamFilter))
                .groupBy(MEMBER.id, MEMBER.nickname, MEMBER.imageUrl, MEMBER.team.shortName)
                .orderBy(score.desc()).limit(limit).fetch();
    }

    /**
     * 특정 유저의 승리 요정 랭킹 점수 및 정보 조회
     *
     * @param m            전체 유저 평균 승률
     * @param c            전체 유저 평균 직관 횟수
     * @param targetMember 조회 대상 유저
     * @param year         기준 연도
     * @param teamFilter   팀 필터
     * @return VictoryFairyRank DTO (단일 유저의 랭킹 정보)
     */
    public VictoryFairyRankParam findMyRanking(
            final double m,
            final double c,
            final Member targetMember,
            final int year,
            final TeamFilter teamFilter
    ) {
        NumberExpression<Long> w = calculateWinCounts(year);
        NumberExpression<Long> n = calculateTotalCountsWithoutDraws(year);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, n);

        NumberExpression<Double> calculatePercent = w.multiply(100.0).divide(n).doubleValue();

        NumberExpression<Double> safeWinPercent = getSafeWinPercent(n, calculatePercent);

        return jpaQueryFactory.select(
                        Projections.constructor(VictoryFairyRankParam.class, score, NICKNAME.value, MEMBER.imageUrl,
                                MEMBER.team.shortName, safeWinPercent))
                .from(MEMBER)
                .join(CHECK_IN).on(CHECK_IN.member.eq(MEMBER), isFavoriteTeam())
                .leftJoin(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year), isMyTeamInGame())
                .leftJoin(TEAM).on(MEMBER.team.eq(TEAM))
                .where(
                        MEMBER.eq(targetMember),
                        filterByTeam(teamFilter),
                        isMemberNotDeleted()
                )
                .groupBy(MEMBER.id, MEMBER.nickname, MEMBER.imageUrl, MEMBER.team.shortName)
                .fetchOne();
    }

    /**
     * 특정 유저의 점수를 기준으로 "내 위에 몇 명이 있는지" 순위를 계산
     * <p>
     * - targetScore보다 점수가 높은 유저 수를 계산하여, "내 순위 = (해당 수 + 1)"로 환산 가능
     *
     * @param targetScore 조회 대상 유저의 베이즈 평균 점수
     * @param m           전체 유저 평균 승률
     * @param c           전체 유저 평균 직관 횟수
     * @param year        기준 연도
     * @param teamFilter  팀 필터
     * @return 나보다 점수가 높은 유저 수 (즉, 내 순위 - 1)
     */
    public int calculateMyRankingOrder(
            final double targetScore,
            final double m,
            final double c,
            final int year,
            final TeamFilter teamFilter
    ) {
        NumberExpression<Long> w = calculateWinCounts(year);
        NumberExpression<Long> n = calculateTotalCountsWithoutDraws(year);
        NumberExpression<Double> score = calculateWinRankingScore(m, c, w, n);

        Expression<Double> myScore = Expressions.constant(targetScore);
        return jpaQueryFactory.selectOne()
                .from(MEMBER)
                .leftJoin(CHECK_IN).on(CHECK_IN.member.eq(MEMBER))
                .leftJoin(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year))
                .leftJoin(TEAM).on(CHECK_IN.team.eq(TEAM))
                .where(filterByTeam(teamFilter), isMemberNotDeleted())
                .groupBy(MEMBER)
                .having(score.gt(myScore))
                .fetch().size();
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


    @Override
    public int findRecentGamesDrawCounts(final Member member, final int year, final int limit) {
        return conditionCountOnRecentGames(member, year, drawCondition(QCheckIn.checkIn, QGame.game), limit);
    }

    // 내 직관 내역 조회
    // 내 응원 팀 여부 관계 o, 게임 완료 여부 관계 x(취소된 경기도 보여줌)
    public List<CheckInGameParam> findCheckInHistory(
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
                        CheckInGameParam.class,
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

    public List<GameWithFanCountsParam> findGamesWithFanCountsByDate(LocalDate date) {
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
                                GameWithFanCountsParam.class,
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
    public List<StadiumCheckInCountParam> findStadiumCheckInCounts(
            Member member,
            int year
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                StadiumCheckInCountParam.class,
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
                        isMyCurrentFavorite(member, CHECK_IN),
                        condition
                )
                .fetchOne();

        return result.intValue();
    }

    private NumberExpression<Double> getSafeWinPercent(
            final NumberExpression<Long> total,
            final NumberExpression<Double> calculatePercent
    ) {
        return new CaseBuilder().when(total.gt(0)).then(calculatePercent).otherwise(0.0);
    }

    private BooleanExpression isMemberNotDeleted() {
        return MEMBER.deletedAt.isNull();
    }

    private NumberExpression<Double> calculateWinRankingScore(
            final double m,
            final double c,
            final NumberExpression<Long> wins,
            final NumberExpression<Long> total
    ) {
        NumberExpression<Double> denominator = total.doubleValue().add(Expressions.constant(c));
        NumberExpression<Double> numerator = wins.doubleValue().add(Expressions.constant(c * m));

        return new CaseBuilder().when(denominator.ne(0.0))
                .then(numerator.divide(denominator))
                .otherwise(0.0);
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

    private BooleanExpression filterByTeam(final TeamFilter teamFilter) {
        if (teamFilter == TeamFilter.ALL) {
            return Expressions.TRUE.isTrue();
        }

        return TEAM.teamCode.eq(teamFilter.name());
    }

    private Long calculateTotalCheckInCount(final int year) {
        return jpaQueryFactory.select(CHECK_IN.count())
                .from(CHECK_IN)
                .join(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year))
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

    private NumberExpression<Long> calculateTotalCountsWithoutDraws(final int year) {
        return new CaseBuilder()
                .when(GAME.gameState.eq(GameState.COMPLETED)
                        .and(GAME.homeScore.ne(GAME.awayScore))
                        .and(isBetweenYear(year)))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private Long calculatePerCheckInCount(final int year) {
        return jpaQueryFactory.select(MEMBER.countDistinct())
                .from(CHECK_IN)
                .join(GAME).on(CHECK_IN.game.eq(GAME), isComplete(), isBetweenYear(year))
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


    private ConstructorExpression<CheckInGameTeamParam> homeTeamResp(QGame g, final QTeam home, Team team) {
        return Projections.constructor(
                CheckInGameTeamParam.class,
                home.teamCode,
                home.shortName,
                g.homeScore,
                new CaseBuilder().when(home.eq(team)).then(true).otherwise(false),
                g.homePitcher
        );
    }

    private ConstructorExpression<CheckInGameTeamParam> awayTeamResp(QGame g, final QTeam away, Team team) {
        return Projections.constructor(
                CheckInGameTeamParam.class,
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

    private BooleanExpression isMyTeamInGame() {
        return MEMBER.team.eq(GAME.homeTeam)
                .or(MEMBER.team.eq(GAME.awayTeam));
    }
}
