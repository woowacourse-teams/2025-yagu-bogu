package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.game.domain.Game;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.StadiumStatsParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long>, CustomCheckInRepository {

    boolean existsByMemberAndGame(Member member, Game game);

    boolean existsByMemberAndGameDateAndCheckInType(Member member, LocalDate date, CheckInType checkInType);

    @Query("""
                SELECT CASE
                         WHEN COUNT(c) = 1 THEN true
                         ELSE false
                       END
                FROM CheckIn c
                WHERE c.member = :member
                  AND c.game.stadium.id = :stadiumId
                  AND c.game.stadium.level = 'MAIN'
            """)
    boolean isFirstMainStadiumVisit(@Param("member") Member member, @Param("stadiumId") Long stadiumId);

    @Query("""
                SELECT new com.yagubogu.stat.dto.StadiumStatsParam(
                           g.stadium.shortName,
                           SUM(CASE WHEN ci.team.id = ci.member.team.id
                                        AND ((ci.team.id = g.awayTeam.id AND g.awayScore > g.homeScore)
                                          OR (ci.team.id = g.homeTeam.id AND g.homeScore > g.awayScore))
                                   THEN 1 ELSE 0 END),
                           SUM(CASE WHEN ci.team.id = ci.member.team.id
                                        AND g.awayScore <> g.homeScore
                                   THEN 1 ELSE 0 END)
                       )
                FROM CheckIn ci
                JOIN ci.game g
                WHERE ci.member.id = :memberId
                  AND g.gameState = 'COMPLETED'
                  AND g.date BETWEEN :startDate AND :endDate
                GROUP BY g.stadium.id
            """)
    List<StadiumStatsParam> findWinAndNonDrawCountByStadium(@Param("memberId") Long memberId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT DISTINCT c.member.id
            FROM CheckIn c
            JOIN c.game g
            WHERE g.date = :date
            ORDER BY c.member.id
            """)
    Slice<Long> findDistinctMemberIdsByDate(
            @Param("date") LocalDate date,
            Pageable pageable
    );

    boolean existsByGameAndMember(Game game, Member member);

    @Query(value = """
            WITH member_counts AS (
               SELECT
                 ci.member_id AS memberId,
                 COUNT(*) AS checkInCount
               FROM check_ins ci
               JOIN games g ON g.game_id = ci.game_id
               JOIN members m ON m.member_id = ci.member_id
               WHERE g.date >= :startAt AND g.date < :endAt
                    AND m.deleted_at IS NULL
                    AND m.team_id IS NOT NULL
               GROUP BY ci.member_id
             ),
             ranked AS (
               SELECT
                 DENSE_RANK() OVER (ORDER BY mc.checkInCount DESC) AS rnk,
                 mc.memberId,
                 mc.checkInCount
               FROM member_counts mc
             )
             SELECT
               r.rnk AS rank_no,
               m.member_id AS memberId,
               m.nickname AS nickname,
               t.short_name AS favoriteTeam,
               m.image_url AS profileImageUrl,
               CAST(r.checkInCount AS DOUBLE) AS score
             FROM ranked r
             JOIN members m ON m.member_id = r.memberId
             JOIN teams t ON t.team_id = m.team_id
             WHERE r.rnk <= :limit
             ORDER BY r.rnk ASC, m.member_id ASC
            """, nativeQuery = true)
    List<LeaderboardRow> findMostCheckInWinner(
            @Param("limit") int limit,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
