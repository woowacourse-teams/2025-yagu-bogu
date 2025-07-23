package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    @Query("""
                SELECT COUNT(ci) 
                FROM CheckIn ci
                JOIN ci.member m
                JOIN ci.game g
                WHERE m = :member
                  AND YEAR(g.date) = :year
                  AND (
                        (g.homeTeam = m.team AND g.homeScore > g.awayScore)
                     OR (g.awayTeam = m.team AND g.awayScore > g.homeScore)
                  )
            """)
    int findWinCounts(Member member, int year);

    @Query("""
                SELECT COUNT(ci) FROM CheckIn ci
                JOIN ci.member m
                JOIN ci.game g
                WHERE m = :member
                  AND YEAR(g.date) = :year
                  AND (
                        (g.homeTeam = m.team AND g.homeScore < g.awayScore)
                     OR (g.awayTeam = m.team AND g.awayScore < g.homeScore)
                  )
            """)
    int findLoseCounts(Member member, int year);

    @Query("""
                SELECT COUNT(ci) FROM CheckIn ci
                JOIN ci.member m
                JOIN ci.game g
                WHERE m = :member
                  AND YEAR(g.date) = :year
                  AND  ((g.homeTeam = m.team OR g.awayTeam = m.team) AND g.homeScore = g.awayScore)
            """)
    int findDrawCounts(Member member, int year);

    int countByGame(Game game);

    @Query("""
                SELECT m.team.id, m.team.name, COUNT(ci)
                FROM CheckIn ci
                JOIN ci.member m
                WHERE ci.game = :game
                GROUP BY m.team.id
            """)
    List<Object[]> countCheckInGroupByTeam(Game game);
}
