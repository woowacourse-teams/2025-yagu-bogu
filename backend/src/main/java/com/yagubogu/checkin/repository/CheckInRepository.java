package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.FanCountsByGameResponse;
import com.yagubogu.checkin.dto.TeamCheckInCountResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingEntryResponse;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
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
            SELECT new com.yagubogu.checkin.dto.TeamCheckInCountResponse(m.team.id, m.team.shortName, COUNT(ci))
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

    @Query("""
                SELECT new com.yagubogu.checkin.dto.CheckInGameResponse(
                    c.id,
                    g.stadium.fullName,
                    new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
                        g.homeTeam.id,
                        g.homeTeam.shortName,
                        g.homeScore,
                        CASE WHEN g.homeTeam = :team THEN true ELSE false END
                    ),
                    new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
                        g.awayTeam.id,
                        g.awayTeam.shortName,
                        g.awayScore,
                        CASE WHEN g.awayTeam = :team THEN true ELSE false END
                    ),
                    g.date
                )
                FROM CheckIn c
                JOIN c.game g
                WHERE c.member = :member
                  AND (g.homeTeam = :team OR g.awayTeam = :team)
                  AND YEAR(g.date) = :year
                ORDER BY g.date DESC
            """)
    List<CheckInGameResponse> findCheckInHistory(Member member, Team team, int year);

    boolean existsByMemberAndGameDate(Member member, LocalDate date);

    @Query("""
                select new com.yagubogu.checkin.dto.VictoryFairyRankingEntryResponse(
                    ci.member.id,
                    ci.member.nickname,
                    ci.member.team.shortName,
                    COUNT(ci),
                    ROUND(
                        (1.0 * SUM(
                            CASE
                                WHEN (g.homeTeam.id = ci.team.id AND g.homeScore > g.awayScore)
                                    OR (g.awayTeam.id = ci.team.id AND g.awayScore > g.homeScore)
                                THEN 1 ELSE 0
                                END
                        ) / COUNT(ci)) * 100, 1
                    )
                )
                from CheckIn ci
                JOIN ci.game g
                group by ci.member.id
            """)
    List<VictoryFairyRankingEntryResponse> findVictoryFairyRankingCandidates();

    @Query("""
            SELECT new com.yagubogu.checkin.dto.CheckInGameResponse(
                c.id,
                g.stadium.fullName,
                new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
                    g.homeTeam.id,
                    g.homeTeam.shortName,
                    g.homeScore,
                    CASE WHEN g.homeTeam = :team THEN true ELSE false END
                ),
                new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
                    g.awayTeam.id,
                    g.awayTeam.shortName,
                    g.awayScore,
                    CASE WHEN g.awayTeam = :team THEN true ELSE false END
                ),
                g.date
            )
            FROM CheckIn c
            JOIN c.game g
            WHERE c.member = :member AND (
                (g.homeTeam = :team AND g.homeScore > g.awayScore)
                    OR
                (g.awayTeam = :team AND g.awayScore > g.homeScore)
            ) AND YEAR(g.date) = :year
            ORDER BY g.date DESC
            """)
    List<CheckInGameResponse> findCheckInWinHistory(Member member, Team team, int year);

    @Query("""
                SELECT new com.yagubogu.checkin.dto.FanCountsByGameResponse(
                    COUNT(c),
                    COALESCE(SUM(CASE WHEN c.team = :homeTeam THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN c.team = :awayTeam THEN 1 ELSE 0 END), 0)
                )
                FROM CheckIn c
                WHERE c.game = :game
            """)
    FanCountsByGameResponse countTotalAndHomeTeamAndAwayTeam(Game game, Team homeTeam, Team awayTeam);

    @Query("""
            SELECT new com.yagubogu.stat.dto.AverageStatistic(
                AVG(
                    CASE
                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.runs
                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.runs
                    END
                ),
                AVG(
                    CASE
                        WHEN g.homeTeam = ci.team THEN g.awayScoreBoard.runs
                        WHEN g.awayTeam = ci.team THEN g.homeScoreBoard.runs
                    END
                ),
                AVG(
                    CASE
                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.errors
                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.errors
                    END
                ),
                AVG(
                    CASE
                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.hits
                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.hits
                    END
                ),
                AVG(
                    CASE
                        WHEN g.homeTeam = ci.team THEN g.awayScoreBoard.hits
                        WHEN g.awayTeam = ci.team THEN g.homeScoreBoard.hits
                    END
                )
            )
            FROM CheckIn ci
            JOIN ci.game g
            WHERE ci.member = :member
                AND (g.homeTeam = ci.team OR g.awayTeam = ci.team)
            """)
    AverageStatistic findAverageStatistic(Member member);
}
