package com.yagubogu.stat.repository;

import com.yagubogu.stat.domain.VictoryFairyRanking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VictoryFairyRankingRepository extends JpaRepository<VictoryFairyRanking, Long>,
        VictoryFairyRankingRepositoryCustom {

    @Modifying(
            flushAutomatically = true, clearAutomatically = true
    )
    @Query(value = """
            INSERT INTO victory_fairy_rankings(member_id, score, win_count, check_in_count, game_year)
            SELECT
              m.member_id,
              COALESCE(ROUND(100.0 * (
                (:winDelta + :c * :m) / NULLIF((:checkInDelta + :c), 0)
              ), 2), 0) AS score,
              :winDelta,
              :checkInDelta,
              :gameYear
            FROM members m
            WHERE m.member_id IN (:memberIds)
            ON DUPLICATE KEY UPDATE
              win_count      = win_count + :winDelta,
              check_in_count = check_in_count + :checkInDelta,
              score = COALESCE(ROUND(100.0 * (
                (win_count + :c * :m) / NULLIF((check_in_count + :c), 0)
              ), 2), 0)
            """, nativeQuery = true)
    int upsertDelta(
            @Param("m") double m,
            @Param("c") double c,
            @Param("memberIds") List<Long> memberIds,
            @Param("winDelta") int winDelta,
            @Param("checkInDelta") int checkInDelta,
            @Param("gameYear") int gameYear
    );

    @Query("""
            SELECT vfr
            FROM VictoryFairyRanking vfr 
            WHERE vfr.member.id IN :memberIds 
            AND vfr.gameYear = :year
            """)
    List<VictoryFairyRanking> findByMemberIdsAndYear(
            @Param("memberIds") List<Long> memberIds,
            @Param("year") int year
    );
}
