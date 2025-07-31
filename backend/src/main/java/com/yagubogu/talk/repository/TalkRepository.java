package com.yagubogu.talk.repository;

import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.TalkResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TalkRepository extends JpaRepository<Talk, Long> {

    @Query("""
            SELECT new com.yagubogu.talk.dto.TalkResponse(
                t.id,
                t.member.id,
                t.member.nickname,
                t.member.team.shortName,
                t.content,
                t.createdAt
            )
            FROM Talk t
            WHERE t.game.id = :gameId
            ORDER BY t.id DESC
            """)
    List<TalkResponse> findLatestTalks(final Long gameId, Pageable pageable);

    @Query("""
            SELECT new com.yagubogu.talk.dto.TalkResponse(
                t.id,
                t.member.id,
                t.member.nickname,
                t.member.team.shortName,
                t.content,
                t.createdAt
            )
            FROM Talk t
            WHERE t.game.id = :gameId AND t.id < :cursorId
            ORDER BY t.id DESC
            """)
    List<TalkResponse> findPreviousTalks(final Long gameId, Long cursorId, Pageable pageable);

    @Query("""
            SELECT new com.yagubogu.talk.dto.TalkResponse(
                t.id,
                t.member.id,
                t.member.nickname,
                t.member.team.shortName,
                t.content,
                t.createdAt
            )
            FROM Talk t
            WHERE t.game.id = :gameId AND t.id > :cursorId
            ORDER BY t.id ASC 
            """)
    List<TalkResponse> findNewTalks(final Long gameId, Long cursorId, Pageable pageable);
}
