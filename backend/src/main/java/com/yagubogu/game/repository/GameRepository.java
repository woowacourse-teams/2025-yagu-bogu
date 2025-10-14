package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.GameWithCheckIn;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByStadiumAndDate(Stadium stadium, LocalDate date);

    Optional<Game> findByGameCode(String gameCode);

    Optional<Game> findByDateAndHomeTeamAndAwayTeamAndStartAt(LocalDate date, com.yagubogu.team.domain.Team homeTeam,
                                                              com.yagubogu.team.domain.Team awayTeam,
                                                              LocalTime startAt);
    @Query("""
            SELECT new com.yagubogu.game.dto.GameWithCheckIn(
                g.id,
                COUNT(c),
                CASE WHEN MAX(CASE WHEN c.member = :member THEN 1 ELSE 0 END) = 1
                             THEN true ELSE false END,
                new com.yagubogu.game.dto.StadiumByGame(
                    g.stadium.id,
                    g.stadium.fullName
                ),
                new com.yagubogu.game.dto.TeamByGame(
                    g.homeTeam.id,
                    g.homeTeam.shortName,
                    g.homeTeam.teamCode
                ),
                new com.yagubogu.game.dto.TeamByGame(
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
    List<GameWithCheckIn> findGamesWithCheckInsByDate(LocalDate date, Member member);

    @Query("""
        select g from Game g
        where g.date = :date
          and g.homeTeam.teamCode = :homeTeamCode
          and g.awayTeam.teamCode = :awayTeamCode
          and g.startAt = :startAt
    """)
    Optional<Game> findByNaturalKey(LocalDate date, String homeTeamCode, String awayTeamCode, LocalTime startAt);
}
