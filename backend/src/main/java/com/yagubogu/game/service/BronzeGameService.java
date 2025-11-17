package com.yagubogu.game.service;

import com.yagubogu.game.domain.BronzeGame;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.BronzeGameRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BronzeGameService {

    private final BronzeGameRepository bronzeGameRepository;

    @Transactional
    public boolean upsertByNaturalKey(
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime,
            final String payload
    ) {
        final String contentHash = calculateHash(payload);
        final LocalDateTime now = LocalDateTime.now();

        return bronzeGameRepository
                .findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                        date, stadium, homeTeam, awayTeam, startTime
                )
                .map(existing -> updateIfHashChanged(existing, payload, contentHash, now))
                .orElseGet(() -> createNewByNaturalKey(
                        date, stadium, homeTeam, awayTeam, startTime,
                        payload, contentHash, now
                ));
    }

    @Transactional
    public boolean updateGameState(
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime,
            final GameState gameState
    ) {
        return bronzeGameRepository
                .findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                        date, stadium, homeTeam, awayTeam, startTime
                )
                .map(existing -> {
                    boolean updated = existing.updateState(gameState);
                    if (updated) {
                        log.info("Bronze gameCenterState updated: date={}, stadium={}, home={}, away={}, state={}",
                                date, stadium, homeTeam, awayTeam, gameState);
                    }
                    return updated;
                })
                .orElseGet(() -> {
                    log.warn("Bronze game not found for GameCenter state update: date={}, stadium={}, home={}, away={}",
                            date, stadium, homeTeam, awayTeam);
                    return false;
                });
    }

    private boolean updateIfHashChanged(final BronzeGame existing, final String payload,
                                        final String contentHash, final LocalDateTime now) {
        if (existing.getContentHash().equals(contentHash)) {
            log.debug("No change detected: date={}, stadium={}, home={}, away={}",
                    existing.getDate(), existing.getStadium(), existing.getHomeTeam(), existing.getAwayTeam());
            return false;
        }

        existing.update(now, payload, contentHash);
        log.info("Bronze updated: date={}, stadium={}, home={}, away={}",
                existing.getDate(), existing.getStadium(), existing.getHomeTeam(), existing.getAwayTeam());
        return true;
    }

    private boolean createNewByNaturalKey(
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime,
            final String payload,
            final String contentHash,
            final LocalDateTime now
    ) {
        final BronzeGame bronzeGame = new BronzeGame(
                date, stadium, homeTeam, awayTeam, startTime,
                now, payload, contentHash
        );
        bronzeGameRepository.save(bronzeGame);
        log.info("Bronze created: date={}, stadium={}, home={}, away={}, startTime={}",
                date, stadium, homeTeam, awayTeam, startTime);
        return true;
    }

    private String calculateHash(final String content) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
