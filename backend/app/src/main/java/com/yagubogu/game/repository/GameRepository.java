package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByGameCode(String gameCode);

    boolean existsByGameCode(String gameCode);

    Optional<Game> findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
            LocalDate date,
            Stadium stadium,
            Team homeTeam,
            Team awayTeam,
            LocalTime startAt
    );

    List<Game> findByDateAndStadiumAndHomeTeamAndAwayTeam(
            LocalDate date,
            Stadium stadium,
            Team homeTeam,
            Team awayTeam
    );

    @Query("""
            SELECT new com.yagubogu.game.dto.GameWithCheckInParam(
                g.id,
                COUNT(c),
                CASE WHEN MAX(CASE WHEN c.member = :member THEN 1 ELSE 0 END) = 1
                             THEN true ELSE false END,
                new com.yagubogu.game.dto.StadiumByGameParam(
                    g.stadium.id,
                    g.stadium.fullName
                ),
                new com.yagubogu.game.dto.TeamByGameParam(
                    g.homeTeam.id,
                    g.homeTeam.shortName,
                    g.homeTeam.teamCode
                ),
                new com.yagubogu.game.dto.TeamByGameParam(
                    g.awayTeam.id,
                    g.awayTeam.shortName,
                    g.awayTeam.teamCode
                ),
                g.startAt
            )
            FROM Game g
            LEFT JOIN CheckIn c ON c.game = g
            WHERE g.date = :date
            GROUP BY g.id
            """)
    List<GameWithCheckInParam> findGamesWithCheckInsByDate(LocalDate date, Member member);

    @Query("SELECT g FROM Game g " +
            "JOIN FETCH g.stadium " +
            "JOIN FETCH g.homeTeam " +
            "JOIN FETCH g.awayTeam " +
            "WHERE g.date = :date")
    List<Game> findAllByDateWithStadium(@Param("date") LocalDate date);

    List<Game> findAllByDate(LocalDate date);

    boolean existsByDateAndGameStateIn(LocalDate date, List<GameState> states);

    @Query("""
            SELECT DISTINCT g.date
            FROM Game g
            WHERE g.date >= :start
                AND g.date < :end
                AND g.gameState <> com.yagubogu.game.domain.GameState.CANCELED
            ORDER BY g.date
            """)
    List<LocalDate> findDistinctGameDatesByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 위젯 스케줄러: 특정 날짜·상태·시작 시간 범위의 경기를 조회합니다.
     * 경기 시작 30분 전 START 푸시 대상 경기를 찾을 때 사용합니다.
     */
    @Query("""
            SELECT g FROM Game g
            JOIN FETCH g.homeTeam
            JOIN FETCH g.awayTeam
            WHERE g.date = :date
              AND g.gameState = :state
              AND g.startAt >= :from
              AND g.startAt < :to
            """)
    List<Game> findScheduledGamesStartingBetween(
            @Param("date") LocalDate date,
            @Param("state") GameState state,
            @Param("from") LocalTime from,
            @Param("to") LocalTime to
    );

    /**
     * 위젯 스케줄러: 종료 처리가 필요한 경기를 조회합니다.
     * START 푸시가 발송된 경기 중 COMPLETED/CANCELED 된 것을 찾을 때 사용합니다.
     */
    @Query("""
            SELECT g FROM Game g
            JOIN FETCH g.homeTeam
            JOIN FETCH g.awayTeam
            WHERE g.date = :date
              AND g.gameState IN :states
              AND g.id IN :gameIds
            """)
    List<Game> findFinalizedGamesById(
            @Param("date") LocalDate date,
            @Param("states") List<GameState> states,
            @Param("gameIds") List<Long> gameIds
    );

    /**
     * 위젯 스케줄러 더블헤더 감지: 특정 날짜에 해당 팀이 LIVE 상태인 경기가 있는지 확인합니다.
     */
    @Query("""
            SELECT COUNT(g) > 0 FROM Game g
            WHERE g.date = :date
              AND g.gameState = com.yagubogu.game.domain.GameState.LIVE
              AND (g.homeTeam = :team OR g.awayTeam = :team)
            """)
    boolean existsLiveGameForTeam(@Param("date") LocalDate date, @Param("team") Team team);
}
