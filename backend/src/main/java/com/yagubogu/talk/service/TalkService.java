package com.yagubogu.talk.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final TalkReportRepository talkReportRepository;

    public CursorResult<TalkResponse> findTalksExcludingReported(
            final long gameId,
            final Long cursorId,
            final int limit,
            final long memberId
    ) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TalkResponse> talkResponses = getTalkResponses(gameId, cursorId, pageable);

        boolean hasNextPage = hasNextPageAndTrim(limit, talkResponses);

        Long nextCursorId = getNextCursorIdOrNull(hasNextPage, talkResponses);
        List<TalkResponse> hiddenReportedTalks = hideReportedTalks(talkResponses, memberId);

        return new CursorResult<>(hiddenReportedTalks, nextCursorId, hasNextPage);
    }

    public CursorResult<TalkResponse> pollTalks(
            final long gameId,
            final long cursorId,
            final int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TalkResponse> talkResponses = talkRepository.fetchTalksAfterCursor(gameId, cursorId, pageable);

        boolean hasNextPage = hasNextPageAndTrim(limit, talkResponses);

        long nextCursorId = getNextCursorIdOrStay(cursorId, talkResponses);

        return new CursorResult<>(talkResponses, nextCursorId, hasNextPage);
    }

    public TalkResponse createTalk(
            final long gameId,
            final TalkRequest request,
            final long memberId // TODO: 나중에 삭제
    ) {
        Game game = getGame(gameId);
        Member member = getMember(memberId);
        LocalDateTime now = LocalDateTime.now();

        Talk talk = talkRepository.save(new Talk(game, member, request.content(), now));

        return TalkResponse.from(talk);
    }

    public void removeTalk(
            final long gameId,
            final long talkId,
            final long memberId // TODO: 나중에 삭제
    ) {
        Talk talk = getTalk(talkId);

        if (talk.getGame().getId() != gameId) {
            throw new BadRequestException("Invalid gameId for the talk");
        }

        if (talk.getMember().getId() != memberId) {
            throw new ForbiddenException("Invalid memberId for the talk");
        }

        talkRepository.deleteById(talkId);
    }

    public List<TalkResponse> hideReportedTalks(
            final List<TalkResponse> talks,
            final long memberId // TODO: 나중에 삭제
    ) {
        if (talks.isEmpty()) {
            return talks;
        }

        List<Long> talkIds = talks.stream()
                .map(TalkResponse::id)
                .toList();

        Set<Long> hiddenTalkIds = new HashSet<>(
                talkReportRepository.findTalkIdsByMemberIdAndTalkIds(memberId, talkIds)
        );

        return talks.stream()
                .map(talk -> hiddenTalkIds.contains(talk.id())
                        ? TalkResponse.hiddenFrom(talk)
                        : talk)
                .toList();
    }

    private List<TalkResponse> getTalkResponses(final long gameId, final Long cursorId, final Pageable pageable) {
        List<TalkResponse> talkResponses;
        if (cursorId == null) {
            talkResponses = talkRepository.fetchRecentTalks(gameId, pageable);
        } else {
            talkResponses = talkRepository.fetchTalksBeforeCursor(gameId, cursorId, pageable);
        }
        return talkResponses;
    }

    private static boolean hasNextPageAndTrim(final int limit, final List<TalkResponse> talkResponses) {
        boolean hasNextPage = talkResponses.size() > limit;
        if (hasNextPage) {
            talkResponses.remove(limit);
        }
        return hasNextPage;
    }

    private static Long getNextCursorIdOrNull(final boolean hasNextPage, final List<TalkResponse> talks) {
        if (!hasNextPage || talks.isEmpty()) {
            return null;
        }
        return talks.getLast().id();
    }

    private static long getNextCursorIdOrStay(final long cursorId, final List<TalkResponse> talks) {
        long nextCursorId;
        if (!talks.isEmpty()) {
            nextCursorId = talks.getLast().id();
        } else {
            nextCursorId = cursorId;
        }
        return nextCursorId;
    }

    private Game getGame(final long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Talk getTalk(final long talkId) {
        return talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));
    }
}
