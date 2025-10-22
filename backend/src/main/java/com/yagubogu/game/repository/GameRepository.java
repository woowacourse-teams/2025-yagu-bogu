package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByStadiumAndDate(Stadium stadium, LocalDate date);

    Optional<Game> findByGameCode(String gameCode);

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
                )
            )
            FROM Game g
            LEFT JOIN CheckIn c ON c.game = g
            WHERE g.date = :date
            GROUP BY g.id
            """)
    List<GameWithCheckInParam> findGamesWithCheckInsByDate(LocalDate date, Member member);

    @Query("""
                select g
                from Game g
                join fetch g.stadium s
                where g.date = :date
            """)
    List<Game> findByDateWithStadium(LocalDate date);

    /**
     * Bulk 상태 조회 (Batch Guard용)
     * 종료 상태(COMPLETED, CANCELED) 경기의 gameCode 조회
     */
    @Query("SELECT g.gameCode FROM Game g " +
            "WHERE g.gameCode IN :gameCodes AND g.gameState IN :states")
    Set<String> findGameCodesByGameCodeInAndGameStateIn(
            @Param("gameCodes") Set<String> gameCodes,
            @Param("states") List<GameState> states
    );

    List<Game> findAllByDate(LocalDate date);

    boolean existsByDateAndGameStateIn(LocalDate date, List<GameState> states);
}
