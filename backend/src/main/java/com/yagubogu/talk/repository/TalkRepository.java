package com.yagubogu.talk.repository;

import com.yagubogu.talk.domain.Talk;
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
            WHERE t.game.id = :gameId AND t.id > :cursorId
            ORDER BY t.id DESC
            """)
    Slice<Talk> fetchTalksAfterCursor(
            @Param("gameId") long gameId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
