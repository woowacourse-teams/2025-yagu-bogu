package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.StadiumStatsParam;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long>, CustomCheckInRepository {

    boolean existsByMemberAndGameDate(Member member, LocalDate date);

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
}
