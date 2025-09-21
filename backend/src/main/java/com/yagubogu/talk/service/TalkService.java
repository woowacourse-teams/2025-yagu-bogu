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
import com.yagubogu.talk.dto.TalkCursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.event.TalkEvent;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TalkService {

    private static final Integer REPORTER_THRESHOLD_FOR_BLOCK = 10;

    private final TalkRepository talkRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final TalkReportRepository talkReportRepository;
    private final ApplicationEventPublisher publisher;

    public TalkCursorResult findTalksExcludingReported(
            final long gameId,
            final Long cursorId,
            final int limit,
            final long memberId
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        Slice<TalkResponse> talkResponses = getTalkResponses(gameId, cursorId, memberId, pageable);

        Long nextCursorId = getNextCursorIdOrNull(talkResponses.hasNext(), talkResponses);
        List<TalkResponse> hiddenReportedTalks = hideReportedTalks(talkResponses.getContent(), memberId);

        Game game = getGame(gameId);
        CursorResult<TalkResponse> cursorResult = new CursorResult<>(hiddenReportedTalks, nextCursorId,
                talkResponses.hasNext());

        return TalkCursorResult.from(game, cursorResult);
    }

    public TalkCursorResult findNewTalks(
            final long gameId,
            final long cursorId,
            final long memberId,
            final int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        Slice<Talk> talks = talkRepository.fetchTalksAfterCursor(gameId, cursorId, pageable);
        Slice<TalkResponse> talkResponses = talks.map(talk -> TalkResponse.from(talk, memberId));

        long nextCursorId = getNextCursorIdOrStay(cursorId, talkResponses);
        Game game = getGame(gameId);
        CursorResult<TalkResponse> cursorResult = new CursorResult<>(talkResponses.getContent(),
                nextCursorId, talkResponses.hasNext());

        return TalkCursorResult.from(game, cursorResult);
    }

    @Transactional
    public TalkResponse createTalk(
            final long gameId,
            final TalkRequest request,
            final long memberId
    ) {
        Game game = getGame(gameId);
        Member member = getMember(memberId);
        LocalDateTime now = LocalDateTime.now();

        validateBlockedFromGame(gameId, memberId);

        Talk talk = talkRepository.save(new Talk(game, member, request.content(), now));
        publisher.publishEvent(new TalkEvent(member));

        return TalkResponse.from(talk, memberId);
    }

    @Transactional
    public void removeTalk(
            final long gameId,
            final long talkId,
            final long memberId
    ) {
        Talk talk = getTalk(talkId);

        if (isValidGameId(gameId, talk)) {
            throw new BadRequestException("Invalid gameId for the talk");
        }

        if (isValidMemberId(memberId, talk)) {
            throw new ForbiddenException("Invalid member for the talk");
        }

        talkRepository.delete(talk);
    }

    public List<TalkResponse> hideReportedTalks(
            final List<TalkResponse> talks,
            final long memberId
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

    private Slice<TalkResponse> getTalkResponses(
            final long gameId,
            final Long cursorId,
            final long memberId,
            final Pageable pageable
    ) {
        if (cursorId == null) {
            Slice<Talk> talks = talkRepository.fetchRecentTalks(gameId, pageable);
            return talks.map(talk -> TalkResponse.from(talk, memberId));
        }
        Slice<Talk> talks = talkRepository.fetchTalksBeforeCursor(gameId, cursorId, pageable);
        return talks.map(talk -> TalkResponse.from(talk, memberId));
    }

    private Long getNextCursorIdOrNull(
            final boolean hasNextPage,
            final Slice<TalkResponse> talks
    ) {
        if (!hasNextPage || talks.isEmpty()) {
            return null;
        }

        return talks.getContent().getLast().id();
    }

    private long getNextCursorIdOrStay(
            final long cursorId,
            final Slice<TalkResponse> talks
    ) {
        if (!talks.isEmpty()) {
            return talks.getContent().getLast().id();
        }

        return cursorId;
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

    private void validateBlockedFromGame(
            final long gameId,
            final long memberId
    ) {
        long distinctReporterCount = talkReportRepository.countDistinctReporterByGameIdAndMemberId(gameId,
                memberId);
        if (distinctReporterCount >= REPORTER_THRESHOLD_FOR_BLOCK) {
            throw new ForbiddenException("Cannot chat due to multiple user reports");
        }
    }

    private boolean isValidGameId(
            final long gameId,
            final Talk talk
    ) {
        return talk.getGame().getId() != gameId;
    }

    private boolean isValidMemberId(
            final long memberId,
            final Talk talk
    ) {
        return talk.getMember().getId() != memberId;
    }
}
