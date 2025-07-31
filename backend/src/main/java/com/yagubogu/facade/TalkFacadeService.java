package com.yagubogu.facade;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.service.TalkService;
import com.yagubogu.talkreport.service.TalkReportService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TalkFacadeService {

    private final TalkService talkService;

    private final TalkReportService talkReportService;

    public CursorResult<TalkResponse> findTalksWithHiddenReport(
            final long gameId,
            final Long cursorId,
            final int limit,
            final long memberId // TODO: 나중에 삭제
    ) {
        CursorResult<TalkResponse> talks = talkService.findTalks(gameId, cursorId, limit);

        List<TalkResponse> talkResponses = talkReportService.hideReportedTalks(talks.content(), memberId);

        return new CursorResult<>(talkResponses, talks.nextCursorId(), talks.hasNext());
    }

    public CursorResult<TalkResponse> pollTalks(
            final long gameId,
            final Long cursorId,
            final int limit
    ) {
        return talkService.pollTalks(gameId, cursorId, limit);
    }

    public TalkResponse createTalk(
            final long gameId,
            final TalkRequest request
    ) {
        return talkService.createTalk(gameId, request);
    }

    public void reportTalk(
            final long talkId,
            final long reporterId // TODO: 나중에 삭제
    ) {
        talkReportService.reportTalk(talkId, reporterId);
    }

    public void removeTalk(
            final long gameId,
            final long talkId,
            final long memberId // TODO: 나중에 삭제
    ) {
        talkService.removeTalk(gameId, talkId, memberId);
    }
}
