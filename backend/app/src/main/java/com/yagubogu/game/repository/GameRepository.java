package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
            LocalDate date,
            Stadium stadium,
            Team homeTeam,
            Team awayTeam,
            LocalTime startAt
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
}
