package com.yagubogu.talk.service;

import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.domain.TalkReport;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TalkReportService {

    private final TalkReportRepository talkReportRepository;
    private final TalkRepository talkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void reportTalk(
            final long talkId,
            final long reporterId
    ) {
        Talk talk = getTalk(talkId);
        Member member = getMember(reporterId);

        validateReportConstraints(talkId, reporterId, talk, member);

        LocalDateTime reportedAt = LocalDateTime.now();

        TalkReport talkReport = new TalkReport(talk, member, reportedAt);
        talkReportRepository.save(talkReport);
    }

    private Talk getTalk(final long talkId) {
        return talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));
    }

    private Member getMember(final long reporterId) {
        return memberRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private void validateReportConstraints(
            final long talkId,
            final long reporterId,
            final Talk talk,
            final Member member
    ) {
        if (talk.getMember().equals(member)) {
            throw new BadRequestException("Cannot report your own comment");
        }
        if (talkReportRepository.existsByTalkIdAndReporterId(talkId, reporterId)) {
            throw new BadRequestException("You have already reported this talk");
        }
    }
}
