package com.yagubogu.like.repository;

import com.yagubogu.like.domain.Like;
import com.yagubogu.like.dto.TeamLikeCountResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO likes (game_id, team_id, total_count)
            VALUES (:gameId, :teamId, :delta)
            ON DUPLICATE KEY UPDATE total_count = total_count + :delta;
            """, nativeQuery = true)
    int upsertDelta(long gameId, long teamId, long delta);

    List<Like> findAllByGameId(long gameId);

    @Query("""
                select new com.yagubogu.like.dto.TeamLikeCountResponse(
                    l.team.id,
                    l.totalCount
                )
                from Like l
                where l.game.id = :gameId
            """)
    List<TeamLikeCountResponse> findTeamCountsByGameId(Long gameId);
}
