package com.yagubogu.talk.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TalkService {

    private final TalkRepository talkRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    public CursorResult<TalkResponse> findTalks(final long gameId, final Long cursorId, final int limit) {
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

    public CursorResult<TalkResponse> pollTalks(final long gameId, final Long cursorId, final int limit) {

        if (cursorId == null) {
            throw new IllegalArgumentException("cursorId는 null일 수 없습니다. 초기로딩을 해주세요.");
        }

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TalkResponse> talkResponses = talkRepository.findNewTalks(gameId, cursorId, pageable);

        boolean hasNextPage = talkResponses.size() > limit;
        if (hasNextPage) {
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

    public TalkResponse createTalk(final long gameId, final TalkRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found."));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Member is not found"));
        LocalDateTime now = LocalDateTime.now();

        Talk talk = talkRepository.save(new Talk(game, member, request.content(), now));

        return new TalkResponse(
                talk.getId(),
                talk.getMember().getId(),
                talk.getMember().getNickname(),
                talk.getMember().getTeam().getShortName(),
                talk.getContent(),
                talk.getCreatedAt()
        );
    }
}
