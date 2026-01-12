package com.yagubogu.like.service;

import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.like.dto.TeamLikeCountParam;
import com.yagubogu.like.dto.v1.LikeBatchRequest;
import com.yagubogu.like.dto.v1.LikeBatchRequest.LikeDelta;
import com.yagubogu.like.dto.v1.LikeCountsResponse;
import com.yagubogu.like.repository.LikeRepository;
import com.yagubogu.like.repository.LikeWindowRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
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
    private final TeamRepository teamRepository;

    @Transactional
    public void applyBatch(
            final long gameId,
            final long memberId,
            final LikeBatchRequest request
    ) {
        existsGame(gameId);

        // 멱등성 키 insert (INSERT IGNORE -> 이미 처리된 배치면 재적용 금지)
        boolean inserted = likeWindowRepository.tryInsertWindow(
                gameId,
                memberId,
                request.windowStartEpochSec()
        );
        if (!inserted) {
            return;
        }

        LikeDelta likeDelta = request.likeDelta();
        Team team = getTeamByCode(likeDelta);
        likeRepository.upsertDelta(gameId, team.getId(), likeDelta.delta());
    }

    public LikeCountsResponse findCounts(final long gameId) {
        existsGame(gameId);

        List<TeamLikeCountParam> teamLikeCounts = likeRepository.findTeamCountsByGameId(gameId);

        return new LikeCountsResponse(gameId, teamLikeCounts);
    }

    private Team getTeamByCode(final LikeDelta likeDelta) {
        return teamRepository.findByTeamCode(likeDelta.teamCode())
                .orElseThrow(() -> new NotFoundException("Team not found"));
    }

    private void existsGame(final long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new NotFoundException("Game not found");
        }
    }
}
