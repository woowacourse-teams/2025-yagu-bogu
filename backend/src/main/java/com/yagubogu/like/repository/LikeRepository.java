package com.yagubogu.like.repository;

import com.yagubogu.like.domain.Like;
import com.yagubogu.like.dto.TeamLikeCountParam;
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

    @Query("""
                select new com.yagubogu.like.dto.TeamLikeCountParam(
                    t.teamCode,
                    case when l.totalCount is null then 0L else l.totalCount end
                )
                from Game g
                join Team t on (t = g.homeTeam or t = g.awayTeam)
                left join Like l on l.game = g and l.team = t
                where g.id = :gameId
            """)
    List<TeamLikeCountParam> findTeamCountsByGameId(Long gameId);
}
