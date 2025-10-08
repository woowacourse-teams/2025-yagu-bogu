package com.yagubogu.stat.repository;

import com.yagubogu.stadium.domain.VictoryFairyRanking;
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
              m.member_id, ROUND(100.0 * (:winDelta + :c * :m) / (1 + :c), 2), :winDelta, 1, :gameYear
            FROM members m
            WHERE m.member_id IN (:memberIds)
            ON DUPLICATE KEY UPDATE
              win_count      = win_count + :winDelta,
              check_in_count = check_in_count + 1,
              score = ROUND(100.0 * (
                win_count + :c * :m
              ) / (
                check_in_count + :c
              ), 2)
            """, nativeQuery = true)
    int upsertDelta(
            @Param("m") double m,
            @Param("c") double c,
            @Param("memberIds") List<Long> memberIds,
            @Param("winDelta") double winDelta,
            @Param("gameYear") int gameYear
    );
}
