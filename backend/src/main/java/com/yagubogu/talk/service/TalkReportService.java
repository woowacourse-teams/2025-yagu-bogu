package com.yagubogu.talk.service;

import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.domain.TalkReport;
import com.yagubogu.talk.repository.TalkReportRepository;
import com.yagubogu.talk.repository.TalkRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TalkReportService {

    private final TalkReportRepository talkReportRepository;

    private final TalkRepository talkRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void reportTalk(
            final long talkId,
            final long reporterId // TODO: 나중에 삭제
    ) {
        Talk talk = talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));
        Member member = memberRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));

        if (talk.getMember().equals(member)) {
            throw new BadRequestException("Cannot report your own comment");
        }
        if (talkReportRepository.existsByTalkIdAndReporterId(talkId, reporterId)) {
            throw new BadRequestException("Talk already exists");
        }

        LocalDateTime reportedAt = LocalDateTime.now();

        TalkReport talkReport = new TalkReport(talk, member, reportedAt);
        talkReportRepository.save(talkReport);
    }
}
