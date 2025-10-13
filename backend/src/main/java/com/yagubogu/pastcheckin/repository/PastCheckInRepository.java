package com.yagubogu.pastcheckin.repository;

import com.yagubogu.pastcheckin.domain.PastCheckIn;
import com.yagubogu.stat.dto.StadiumStatsDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PastCheckInRepository extends JpaRepository<PastCheckIn, Long> {

    @Query("""
                SELECT new com.yagubogu.stat.dto.StadiumStatsDto(
                           g.stadium.shortName,
                           SUM(CASE WHEN pci.team.id = pci.member.team.id
                                        AND ((pci.team.id = g.awayTeam.id AND g.awayScore > g.homeScore)
                                          OR (pci.team.id = g.homeTeam.id AND g.homeScore > g.awayScore))
                                   THEN 1 ELSE 0 END),
                           SUM(CASE WHEN pci.team.id = pci.member.team.id
                                        AND g.awayScore <> g.homeScore
                                   THEN 1 ELSE 0 END)
                       )
                FROM PastCheckIn pci
                JOIN pci.game g
                WHERE pci.member.id = :memberId
                  AND g.gameState = 'COMPLETED'
                  AND g.date BETWEEN :startDate AND :endDate
                GROUP BY g.stadium.id, g.stadium.shortName
            """)
    List<StadiumStatsDto> findWinAndNonDrawCountByStadiumFromPastCheckIn(@Param("memberId") Long memberId,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);
}
