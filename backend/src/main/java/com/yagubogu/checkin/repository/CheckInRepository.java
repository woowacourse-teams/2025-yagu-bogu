package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.VictoryFairyRankingEntryResponse;
import com.yagubogu.member.domain.Member;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long>, CustomCheckInRepository {

    boolean existsByMemberAndGameDate(Member member, LocalDate date);

    @Query("""
            select new com.yagubogu.checkin.dto.VictoryFairyRankingEntryResponse(
              ci.member.id,
              ci.member.nickname,
              ci.member.imageUrl,
              ci.member.team.shortName,
              COUNT(CASE WHEN g.homeTeam.id = ci.team.id OR g.awayTeam.id = ci.team.id THEN 1 END),
              CASE
                WHEN COUNT(CASE WHEN g.homeTeam.id = ci.team.id OR g.awayTeam.id = ci.team.id THEN 1 END) = 0
                  THEN 0.0
                ELSE ROUND(
                  (1.0 * SUM(
                    CASE
                      WHEN (g.homeTeam.id = ci.team.id AND g.homeScore > g.awayScore)
                        OR (g.awayTeam.id = ci.team.id AND g.awayScore > g.homeScore)
                      THEN 1 ELSE 0
                    END
                  ) / COUNT(
                    CASE WHEN g.homeTeam.id = ci.team.id OR g.awayTeam.id = ci.team.id THEN 1 END
                  )) * 100,
                  1
                )
              END
            )
            from CheckIn ci
            join ci.game g
            group by
              ci.member.id
            """)
    List<VictoryFairyRankingEntryResponse> findVictoryFairyRankingCandidates();

//    @Query("""
//            SELECT new com.yagubogu.checkin.dto.CheckInGameResponse(
//                c.id,
//                g.stadium.fullName,
//                new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
//                    g.homeTeam.teamCode,
//                    g.homeTeam.shortName,
//                    g.homeScoreBoard.runs,
//                    CASE WHEN g.homeTeam = :team THEN true ELSE false END,
//                    g.homePitcher
//                ),
//                new com.yagubogu.checkin.dto.CheckInGameTeamResponse(
//                    g.awayTeam.teamCode,
//                    g.awayTeam.shortName,
//                    g.awayScoreBoard.runs,
//                    CASE WHEN g.awayTeam = :team THEN true ELSE false END,
//                    g.awayPitcher
//                ),
//                g.date,
//                g.homeScoreBoard,
//                g.awayScoreBoard
//            )
//            FROM CheckIn c
//            JOIN c.game g
//            WHERE c.member = :member AND (
//                (g.homeTeam = :team AND g.homeScore > g.awayScore)
//                    OR
//                (g.awayTeam = :team AND g.awayScore > g.homeScore)
//            )
//                  AND YEAR(g.date) = :year
//            ORDER BY g.date DESC
//            """)
//    List<CheckInGameResponse> findCheckInWinHistory(Member member, Team team, int year);

//    @Query("""
//            SELECT new com.yagubogu.stat.dto.AverageStatistic(
//                AVG(
//                    CASE
//                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.runs
//                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.runs
//                    END
//                ),
//                AVG(
//                    CASE
//                        WHEN g.homeTeam = ci.team THEN g.awayScoreBoard.runs
//                        WHEN g.awayTeam = ci.team THEN g.homeScoreBoard.runs
//                    END
//                ),
//                AVG(
//                    CASE
//                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.errors
//                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.errors
//                    END
//                ),
//                AVG(
//                    CASE
//                        WHEN g.homeTeam = ci.team THEN g.homeScoreBoard.hits
//                        WHEN g.awayTeam = ci.team THEN g.awayScoreBoard.hits
//                    END
//                ),
//                AVG(
//                    CASE
//                        WHEN g.homeTeam = ci.team THEN g.awayScoreBoard.hits
//                        WHEN g.awayTeam = ci.team THEN g.homeScoreBoard.hits
//                    END
//                )
//            )
//            FROM CheckIn ci
//            JOIN ci.game g
//            WHERE ci.member = :member
//                AND (g.homeTeam = ci.team OR g.awayTeam = ci.team)
//            """)
//    AverageStatistic findAverageStatistic(Member member);

//    @Query("""
//             SELECT new com.yagubogu.checkin.dto.StadiumCheckInCountResponse(
//                 s.id,
//                 s.location,
//                 COUNT(c.id)
//             )
//             FROM Stadium s
//             LEFT JOIN CheckIn c ON c.game.stadium.id = s.id
//                                AND c.member = :member
//                                AND c.game.date BETWEEN :startDate AND :endDate
//             GROUP BY s.id, s.location
//            """)
//    List<StadiumCheckInCountResponse> findStadiumCheckInCounts(
//            Member member,
//            LocalDate startDate,
//            LocalDate endDate
//    );

//    @Query("""
//            select new com.yagubogu.stat.dto.OpponentWinRateRow(
//                away.id, away.name, away.shortName, away.teamCode,
//                sum(case when g.homeScore > g.awayScore then 1 else 0 end),
//                sum(case when g.homeScore < g.awayScore then 1 else 0 end),
//                sum(case when g.homeScore = g.awayScore then 1 else 0 end)
//            )
//            from CheckIn ci
//            join ci.game g
//            join Team away on away.id = g.awayTeam.id
//            where ci.member.id = :memberId
//              and ci.team.id = :myTeamId
//              and g.homeTeam.id = :myTeamId
//              and g.date between :start and :end
//              and g.gameState = 'COMPLETED'
//            group by away.id, away.name, away.shortName, away.teamCode
//            """)
//    List<OpponentWinRateRow> findOpponentWinRatesWhenHome(
//            @Param("memberId") Long memberId,
//            @Param("myTeamId") Long myTeamId,
//            @Param("start") LocalDate start,
//            @Param("end") LocalDate end
//    );
//
//    @Query("""
//            select new com.yagubogu.stat.dto.OpponentWinRateRow(
//                home.id, home.name, home.shortName, home.teamCode,
//                sum(case when g.awayScore > g.homeScore then 1 else 0 end),
//                sum(case when g.awayScore < g.homeScore then 1 else 0 end),
//                sum(case when g.awayScore = g.homeScore then 1 else 0 end)
//            )
//            from CheckIn ci
//            join ci.game g
//            join Team home on home.id = g.homeTeam.id
//            where ci.member.id = :memberId
//              and ci.team.id = :myTeamId
//              and g.awayTeam.id = :myTeamId
//              and g.date between :start and :end
//              and g.gameState = 'COMPLETED'
//            group by home.id, home.name, home.shortName, home.teamCode
//            """)
//    List<OpponentWinRateRow> findOpponentWinRatesWhenAway(
//            @Param("memberId") Long memberId,
//            @Param("myTeamId") Long myTeamId,
//            @Param("start") LocalDate start,
//            @Param("end") LocalDate end
//    );
}
