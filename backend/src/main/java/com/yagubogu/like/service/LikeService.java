package com.yagubogu.like.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.like.domain.Like;
import com.yagubogu.like.dto.LikeBatchRequest;
import com.yagubogu.like.dto.LikeCountsResponse;
import com.yagubogu.like.repository.LikeRepository;
import com.yagubogu.like.repository.LikeWindowRepository;
import com.yagubogu.team.domain.Team;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeWindowRepository likeWindowRepository;
    private final GameRepository gameRepository;


    @Transactional
    public LikeCountsResponse applyBatch(final long gameId, final LikeBatchRequest request) {
        Game game = getGame(gameId);

        // 멱등성 키 insert (INSERT IGNORE -> 이미 처리된 배치면 재적용 금지)
        boolean inserted = likeWindowRepository.tryInsertWindow(
                gameId,
                request.clientInstanceId(),
                request.windowStartEpochSec()
        );
        if (!inserted) {
            return buildCountsResponse(gameId);
        }

        Set<Long> validTeamIds = fetchParticipantTeamIds(game);

        for (LikeBatchRequest.Entry e : request.entries()) {
            if (!validTeamIds.contains(e.teamId())) {
                continue;
            }
            likeRepository.upsertDelta(gameId, e.teamId(), e.delta().longValue());
        }

        return buildCountsResponse(gameId);
    }

    @Transactional(readOnly = true)
    public LikeCountsResponse findCounts(final long gameId) {
        Game game = getGame(gameId);

        return buildCountsResponse(game.getId());
    }

    private LikeCountsResponse buildCountsResponse(final long gameId) {
        List<Like> rows = likeRepository.findAllByGameId(gameId);
        Map<Long, Long> counts = rows.stream()
                .collect(Collectors.toMap(l -> l.getTeam().getId(), Like::getTotalCount));

        return new LikeCountsResponse(gameId, counts);
    }

    private Game getGame(final long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not Found"));
    }

    private Set<Long> fetchParticipantTeamIds(final Game game) {
        Team homeTeam = game.getHomeTeam();
        Team awayTeam = game.getAwayTeam();
        return Set.of(homeTeam.getId(), awayTeam.getId());
    }
}
