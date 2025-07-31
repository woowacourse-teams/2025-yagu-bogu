package com.yagubogu.talk.service;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TalkService {

    private final TalkRepository talkRepository;

    public CursorResult<TalkResponse> findTalks(final Long gameId, final Long cursorId, final int limit) {
        List<TalkResponse> talkResponses;
        Pageable pageable = PageRequest.of(0, limit + 1);

        if (cursorId == null) {
            talkResponses = talkRepository.findLatestTalks(gameId, pageable);
        } else {
            talkResponses = talkRepository.findPreviousTalks(gameId, cursorId, pageable);
        }

        boolean hasNextPage = talkResponses.size() > limit;
        if (hasNextPage) {
            talkResponses.remove(limit);
        }

        Long nextCursorId = hasNextPage ? talkResponses.get(talkResponses.size() - 1).id() : null;
        return new CursorResult<>(talkResponses, nextCursorId, hasNextPage);
    }

    public CursorResult<TalkResponse> pollTalks(final Long gameId, final Long cursorId, final int limit) {

        if (cursorId == null) {
            throw new IllegalArgumentException("cursorId는 null일 수 없습니다. 초기로딩을 해주세요.");
        }

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TalkResponse> talkResponses = talkRepository.findNewTalks(gameId, cursorId, pageable);

        boolean hasNextPage = talkResponses.size() > limit;
        if (hasNextPage) { // 10개 새메세지 있으면?
            talkResponses.remove(limit);
        }

        Long nextCursorId;
        if (!talkResponses.isEmpty()) {
            nextCursorId = talkResponses.get(talkResponses.size() - 1).id();
        } else {
            nextCursorId = cursorId;
        }

        return new CursorResult<>(talkResponses, nextCursorId, hasNextPage);
    }
}
