package com.yagubogu.like.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.like.domain.Like;
import com.yagubogu.like.dto.LikeBatchRequest;
import com.yagubogu.like.dto.LikeBatchRequest.LikeDelta;
import com.yagubogu.like.dto.LikeCountsResponse;
import com.yagubogu.like.dto.TeamLikeCountResponse;
import com.yagubogu.like.repository.LikeRepository;
import com.yagubogu.like.repository.LikeWindowRepository;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeWindowRepository likeWindowRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LikeCountsResponse applyBatch(
            final long gameId,
            final LikeBatchRequest request
    ) {
        getGame(gameId);

        // 멱등성 키 insert (INSERT IGNORE -> 이미 처리된 배치면 재적용 금지)
        boolean inserted = likeWindowRepository.tryInsertWindow(
                gameId,
                request.clientInstanceId(),
                request.windowStartEpochSec()
        );
        if (!inserted) {
            return buildCountsResponse(gameId);
        }

        LikeDelta likeDelta = request.likeDelta();
        likeRepository.upsertDelta(gameId, likeDelta.teamId(), likeDelta.delta().longValue());

        return buildCountsResponse(gameId);
    }

    @Transactional(readOnly = true)
    public LikeCountsResponse findCounts(final long gameId, final long memberId) {
        Game game = getGame(gameId);
        Member member = getMember(memberId);

        List<TeamLikeCountResponse> teamLikeCounts = likeRepository.findTeamCountsByGameId(gameId);

        return new LikeCountsResponse(gameId, teamLikeCounts);
    }

    private LikeCountsResponse buildCountsResponse(final long gameId) {
        List<Like> rows = likeRepository.findAllByGameId(gameId);

        List<TeamLikeCountResponse> teamLikeCounts = rows.stream()
                .map(like -> new TeamLikeCountResponse(
                        like.getTeam().getId(),
                        like.getTotalCount()
                ))
                .toList();

        return new LikeCountsResponse(gameId, teamLikeCounts);
    }

    private Game getGame(final long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not Found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException("Member not Found"));
    }
}
