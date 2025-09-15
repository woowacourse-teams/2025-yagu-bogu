package com.yagubogu.like.controller;

import com.yagubogu.like.dto.LikeBatchRequest;
import com.yagubogu.like.dto.LikeCountsResponse;
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
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{gameId}/likes/batch")
    public ResponseEntity<LikeCountsResponse> applyLikeBatch(
            @PathVariable long gameId,
            @RequestBody LikeBatchRequest body
    ) {
        LikeCountsResponse response = likeService.applyBatch(gameId, body);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}/likes/counts")
    public ResponseEntity<LikeCountsResponse> findLikeCounts(@PathVariable long gameId) {
        LikeCountsResponse response = likeService.findCounts(gameId);

        return ResponseEntity.ok(response);
    }
}
