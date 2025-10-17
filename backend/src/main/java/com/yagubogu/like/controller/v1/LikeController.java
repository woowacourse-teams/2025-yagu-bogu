package com.yagubogu.like.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.like.dto.v1.LikeBatchRequest;
import com.yagubogu.like.dto.v1.LikeCountsResponse;
import com.yagubogu.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/games")
@RestController
public class LikeController implements LikeControllerInterface {

    private final LikeService likeService;

    @RequireRole
    @PostMapping("/{gameId}/like-batches")
    public ResponseEntity<Void> applyLikeBatch(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestBody final LikeBatchRequest body
    ) {
        likeService.applyBatch(gameId, memberClaims.id(), body);

        return ResponseEntity.noContent().build();
    }

    @RequireRole
    @GetMapping("/{gameId}/likes/counts")
    public ResponseEntity<LikeCountsResponse> findLikeCounts(@PathVariable final long gameId) {
        LikeCountsResponse response = likeService.findCounts(gameId);

        return ResponseEntity.ok(response);
    }
}
