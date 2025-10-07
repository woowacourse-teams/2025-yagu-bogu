package com.yagubogu.stat.repository;

import com.yagubogu.stadium.domain.VictoryFairyRanking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VictoryFairyRankingRepository extends JpaRepository<VictoryFairyRanking, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
                INSERT IGNORE INTO victory_fairy_rankings (member_id, score, win_count, check_in_count, game_year)
                SELECT m.member_id, 0, 0, 0, :year
                FROM members m
                WHERE m.member_id IN (:memberIds)
            """, nativeQuery = true)
    void upsertMembers(List<Long> checkInMembers, int year);

}
