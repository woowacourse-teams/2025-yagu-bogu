package com.yagubogu.talkreport.service;

import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.talkreport.domain.TalkReport;
import com.yagubogu.talkreport.repository.TalkReportRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TalkReportService {

    private final TalkReportRepository talkReportRepository;

    private final TalkRepository talkRepository;

    private final MemberRepository memberRepository;

    public void reportTalk(
            final long talkId,
            final long reporterId // TODO: 나중에 삭제
    ) {
        Talk talk = talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));
        Member member = memberRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));

        if (talkReportRepository.existsByTalkIdAndReporterId(talkId, reporterId)) {
            throw new BadRequestException("Talk already exists");
        }

        LocalDateTime reportedAt = LocalDateTime.now();

        TalkReport talkReport = new TalkReport(talk, member, reportedAt);
        talkReportRepository.save(talkReport);
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
