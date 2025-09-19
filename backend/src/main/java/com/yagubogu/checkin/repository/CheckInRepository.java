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
              ci.member.nickname.value,
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
            where ci.member.deletedAt IS NULL
            group by
              ci.member.id
            """)
    List<VictoryFairyRankingEntryResponse> findVictoryFairyRankingCandidates();
}
