package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.TeamCheckInCountResponse;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
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
    int findWinCounts(Member member, final int year);

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
    int findLoseCounts(Member member, final int year);

    @Query("""
                SELECT COUNT(ci) FROM CheckIn ci
                JOIN ci.member m
                JOIN ci.game g
                WHERE m = :member
                  AND YEAR(g.date) = :year
                  AND  ((g.homeTeam = m.team OR g.awayTeam = m.team) AND g.homeScore = g.awayScore)
            """)
    int findDrawCounts(Member member, final int year);

    @Query("""
                SELECT COUNT(c)
                FROM CheckIn c
                WHERE c.member = :member
                  AND c.game.stadium = :stadium
                  AND YEAR(c.game.date) = :year
                  AND (
                      (c.member.team = c.game.homeTeam AND c.game.homeScore > c.game.awayScore) OR
                      (c.member.team = c.game.awayTeam AND c.game.awayScore > c.game.homeScore)
                  )
            """)
    int findWinCountsByStadiumAndMember(Stadium stadium, Member member, int year);

    @Query("""
                SELECT COUNT(c)
                FROM CheckIn c
                WHERE c.member = :member
                  AND c.game.stadium = :stadium
                  AND YEAR(c.game.date) = :year
                  AND (
                      c.member.team = c.game.homeTeam OR c.member.team = c.game.awayTeam
                  )
            """)
    int findFavoriteCheckInCountsByStadiumAndMember(Stadium stadium, Member member, int year);

    int countByGame(Game game);

    @Query("""
            SELECT new com.yagubogu.checkin.dto.TeamCheckInCountResponse(m.team.id, m.team.name, COUNT(ci))
            FROM CheckIn ci
            JOIN ci.member m
            WHERE ci.game = :game
            GROUP BY m.team.id
            ORDER BY COUNT(ci) DESC
            """)
    List<TeamCheckInCountResponse> countCheckInGroupByTeam(Game game);

    @Query("""
                SELECT COUNT(c)
                FROM CheckIn c
                WHERE c.member = :member
                  AND YEAR(c.game.date) = :year
            """)
    int countByMemberAndYear(Member member, long year);
}
