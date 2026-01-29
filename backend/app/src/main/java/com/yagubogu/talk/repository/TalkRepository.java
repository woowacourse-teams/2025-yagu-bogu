package com.yagubogu.talk.repository;

import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import com.yagubogu.talk.domain.Talk;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TalkRepository extends JpaRepository<Talk, Long> {

    @Query("""
            SELECT t
            FROM Talk t
            LEFT JOIN t.member m
            WHERE t.game.id = :gameId
            ORDER BY t.id DESC
            """)
    Slice<Talk> fetchRecentTalks(
            @Param("gameId") long gameId,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Talk t
            LEFT JOIN t.member m
            WHERE t.game.id = :gameId AND t.id < :cursorId
            ORDER BY t.id DESC
            """)
    Slice<Talk> fetchTalksBeforeCursor(
            @Param("gameId") long gameId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Talk t
            LEFT JOIN t.member m
            WHERE t.game.id = :gameId AND t.id > :cursorId
            ORDER BY t.id DESC
            """)
    Slice<Talk> fetchTalksAfterCursor(
            @Param("gameId") long gameId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    Optional<Talk> findByClientMessageId(String clientMessageId);

    @Query("""
            SELECT COUNT(t) > 0
            FROM Talk t
            WHERE t.game.id = :gameId
              AND t.member.id = :memberId
              AND t.content = :content
              AND t.createdAt > :threshold
            """)
    boolean existsRecentDuplicate(
            @Param("gameId") long gameId,
            @Param("memberId") long memberId,
            @Param("content") String content,
            @Param("threshold") LocalDateTime threshold
    );

    @Query(value = """
            WITH member_counts AS (
              SELECT
                tk.member_id AS memberId,
                COUNT(*) AS talkCount
              FROM talks tk
              JOIN members m ON m.member_id = tk.member_id
              WHERE
                tk.created_at >= '2025-01-01' AND tk.created_at < '2026-01-01'
                AND m.deleted_at IS NULL
                AND m.team_id IS NOT NULL
                AND NOT EXISTS (
                  SELECT 1
                  FROM talk_reports tr
                  WHERE tr.talk_id = tk.talk_id
                )
              GROUP BY tk.member_id
            ),
            ranked AS (
              SELECT
                DENSE_RANK() OVER (ORDER BY talkCount DESC) AS `rank`,
                memberId
              FROM member_counts
            )
            SELECT
              r.`rank` AS `rank`,
              m.member_id AS memberId,
              m.nickname AS nickname,
              t.short_name AS favoriteTeam,
              m.image_url AS profileImageUrl
            FROM ranked r
            JOIN members m ON m.member_id = r.memberId
            JOIN teams t   ON t.team_id = m.team_id
            WHERE r.`rank` <= :limit
            ORDER BY r.`rank` ASC, m.member_id ASC
            """, nativeQuery = true)
    List<LeaderboardItemResponse> findChattiestWinner(@Param("limit") int limit);
}
