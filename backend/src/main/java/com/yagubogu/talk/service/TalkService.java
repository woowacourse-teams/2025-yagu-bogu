package com.yagubogu.talk.service;

import com.sun.jdi.request.DuplicateRequestException;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.CursorResultParam;
import com.yagubogu.talk.dto.event.TalkEvent;
import com.yagubogu.talk.dto.v1.TalkCursorResultResponse;
import com.yagubogu.talk.dto.v1.TalkEntranceResponse;
import com.yagubogu.talk.dto.v1.TalkRequest;
import com.yagubogu.talk.dto.v1.TalkResponse;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class TalkService {

    private static final Integer REPORTER_THRESHOLD_FOR_BLOCK = 10;
    private static final int DUPLICATE_CHECK_WINDOW_SECONDS = 3;

    private final TalkRepository talkRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final TalkReportRepository talkReportRepository;
    private final ApplicationEventPublisher publisher;
    private final EntityManager entityManager;

    public TalkEntranceResponse findInitialTalksExcludingReported(
            final long gameId,
            final long memberId
    ) {
        Game game = getGame(gameId);
        Member member = getMember(memberId);

        return TalkEntranceResponse.from(game, member);
    }

    public TalkCursorResultResponse findTalksExcludingReported(
            final long gameId,
            final Long cursorId,
            final int limit,
            final long memberId
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        Slice<TalkResponse> talkResponses = getTalkResponses(gameId, cursorId, memberId, pageable);

        Long nextCursorId = getNextCursorIdOrNull(talkResponses.hasNext(), talkResponses);
        List<TalkResponse> hiddenReportedTalks = hideReportedTalks(talkResponses.getContent(), memberId);

        CursorResultParam<TalkResponse> cursorResultParam = new CursorResultParam<>(hiddenReportedTalks, nextCursorId,
                talkResponses.hasNext());

        return new TalkCursorResultResponse(cursorResultParam);
    }

    public TalkCursorResultResponse findNewTalks(
            final long gameId,
            final long cursorId,
            final long memberId,
            final int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        Slice<Talk> talks = talkRepository.fetchTalksAfterCursor(gameId, cursorId, pageable);
        Slice<TalkResponse> talkResponses = talks.map(talk -> TalkResponse.from(talk, memberId));

        long nextCursorId = getNextCursorIdOrStay(cursorId, talkResponses);
        CursorResultParam<TalkResponse> cursorResultParam = new CursorResultParam<>(talkResponses.getContent(),
                nextCursorId, talkResponses.hasNext());

        return new TalkCursorResultResponse(cursorResultParam);
    }

    @Transactional
    public TalkResponse createTalk(
            final long gameId,
            final TalkRequest request,
            final long memberId
    ) {
        // 1. 중복 체크
        Optional<Talk> existingTalk = talkRepository.findByClientMessageId(request.clientMessageId());
        if (existingTalk.isPresent()) {
            log.info("중복 요청 감지 (Client Message ID) - clientMessageId: {}, talkId: {}",
                    request.clientMessageId(), existingTalk.get().getId());
            return TalkResponse.from(existingTalk.get(), memberId);
        }

        // 2~4. 검증
        Game game = getGame(gameId);
        Member member = getMember(memberId);
        validateBlockedFromGame(gameId, memberId);
        validateRecentDuplicate(gameId, memberId, request.content());

        LocalDateTime now = LocalDateTime.now();

        try {
            Talk talk = talkRepository.save(new Talk(
                    request.clientMessageId(),
                    game,
                    member,
                    request.content(),
                    now
            ));
            TalkResponse response = TalkResponse.from(talk, memberId);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.info("트랜잭션 커밋 완료 - 이벤트 발행 시작, thread: {}",
                                    Thread.currentThread().getName());
                            publisher.publishEvent(new TalkEvent(member));
                        }
                    }
            );

            return response;

        } catch (DataIntegrityViolationException e) {
            entityManager.clear();
            throw new DuplicateRequestException("이미 처리된 요청입니다");
        }
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

    private void validateRecentDuplicate(long gameId, long memberId, String content) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(DUPLICATE_CHECK_WINDOW_SECONDS);

        boolean exists = talkRepository.existsRecentDuplicate(gameId, memberId, content, threshold);

        if (exists) {
            log.warn("중복 내용 감지 - gameId: {}, memberId: {}, content: {}",
                    gameId, memberId, content);
            throw new ConflictException("같은 메시지를 너무 빠르게 보내고 있습니다");
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
