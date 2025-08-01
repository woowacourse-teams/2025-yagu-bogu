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
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TalkService {

    private final TalkRepository talkRepository;

    private final GameRepository gameRepository;

    private final MemberRepository memberRepository;

    private final TalkReportRepository talkReportRepository;

    public CursorResult<TalkResponse> findTalks(
            final long gameId,
            final Long cursorId,
            final int limit
    ) {
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

    public CursorResult<TalkResponse> pollTalks(
            final long gameId,
            final Long cursorId,
            final int limit
    ) {
        if (cursorId == null) {
            throw new BadRequestException("cursorId is null");
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

    @Transactional
    public TalkResponse createTalk(
            final long gameId,
            final TalkRequest request,
            final long memberId // TODO: 나중에 삭제
    ) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
        Member member = memberRepository.findById(memberId)
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

    @Transactional
    public void removeTalk(
            final long gameId,
            final long talkId,
            final long memberId // TODO: 나중에 삭제
    ) {
        Talk talk = talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));

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
                        ? new TalkResponse(
                        talk.id(),
                        talk.memberId(),
                        talk.nickname(),
                        talk.favorite(),
                        "숨김처리되었습니다",
                        talk.createdAt())
                        : talk)
                .toList();
    }
}
