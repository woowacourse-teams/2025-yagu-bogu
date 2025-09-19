package com.yagubogu.like.service;

import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.like.dto.LikeBatchRequest;
import com.yagubogu.like.dto.LikeBatchRequest.LikeDelta;
import com.yagubogu.like.dto.LikeCountsResponse;
import com.yagubogu.like.dto.TeamLikeCountResponse;
import com.yagubogu.like.repository.LikeRepository;
import com.yagubogu.like.repository.LikeWindowRepository;
import com.yagubogu.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeWindowRepository likeWindowRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void applyBatch(
            final long gameId,
            final LikeBatchRequest request
    ) {
        existsGame(gameId);

        // 멱등성 키 insert (INSERT IGNORE -> 이미 처리된 배치면 재적용 금지)
        boolean inserted = likeWindowRepository.tryInsertWindow(
                gameId,
                request.clientInstanceId(),
                request.windowStartEpochSec()
        );
        if (!inserted) {
            return;
        }

        LikeDelta likeDelta = request.likeDelta();
        likeRepository.upsertDelta(gameId, likeDelta.teamId(), likeDelta.delta());
    }

    public LikeCountsResponse findCounts(final long gameId, final long memberId) {
        existsGame(gameId);
        existsMember(memberId);

        List<TeamLikeCountResponse> teamLikeCounts = likeRepository.findTeamCountsByGameId(gameId);

        return new LikeCountsResponse(gameId, teamLikeCounts);
    }

    private void existsGame(final long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new NotFoundException("Game not found");
        }
    }

    private void existsMember(final long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException("Member not found");
        }
    }
}
