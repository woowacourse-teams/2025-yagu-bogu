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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
            final String gameCode,
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime,
            final String payload
    ) {
        final String contentHash = calculateHash(payload);
        final LocalDateTime now = LocalDateTime.now();

        return findExisting(gameCode, date, stadium, homeTeam, awayTeam, startTime)
                .map(existing -> updateIfHashChanged(
                        existing, gameCode, date, stadium, homeTeam, awayTeam, startTime,
                        payload, contentHash, now
                ))
                .orElseGet(() -> createNewByNaturalKey(
                        gameCode, date, stadium, homeTeam, awayTeam, startTime,
                        payload, contentHash, now
                ));
    }

    private Optional<BronzeGame> findExisting(
            final String gameCode,
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime
    ) {
        if (gameCode != null && !gameCode.isBlank()) {
            Optional<BronzeGame> byGameCode = bronzeGameRepository.findByGameCode(gameCode);
            if (byGameCode.isPresent()) {
                return byGameCode;
            }
        }

        Optional<BronzeGame> byNaturalKey = bronzeGameRepository
                .findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                date, stadium, homeTeam, awayTeam, startTime
        );
        if (byNaturalKey.isPresent()) {
            return byNaturalKey;
        }

        List<BronzeGame> sameMatchup = bronzeGameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeam(
                date, stadium, homeTeam, awayTeam
        );
        if (sameMatchup.size() == 1) {
            log.info("Bronze matched without startTime: gameCode={}, date={}, stadium={}, home={}, away={}",
                    gameCode, date, stadium, homeTeam, awayTeam);
            return Optional.of(sameMatchup.getFirst());
        }
        return Optional.empty();
    }

    @Transactional
    public boolean updateGameState(
            final String gameCode,
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime,
            final GameState gameState
    ) {
        return findExisting(gameCode, date, stadium, homeTeam, awayTeam, startTime)
                .map(existing -> {
                    boolean metadataUpdated = existing.updateMetadata(
                            gameCode, date, stadium, homeTeam, awayTeam, startTime, LocalDateTime.now()
                    );
                    boolean stateUpdated = existing.updateState(gameState);
                    boolean updated = metadataUpdated || stateUpdated;
                    if (updated) {
                        log.info("Bronze GameCenter data updated: gameCode={}, date={}, stadium={}, home={}, away={}, "
                                        + "startTime={}, state={}, metadataChanged={}",
                                gameCode, date, stadium, homeTeam, awayTeam, startTime, gameState, metadataUpdated);
                    }
                    return updated;
                })
                .orElseGet(() -> {
                    log.warn("Bronze game not found for GameCenter state update: date={}, stadium={}, home={}, away={}",
                            date, stadium, homeTeam, awayTeam);
                    return false;
                });
    }

    private boolean updateIfHashChanged(final BronzeGame existing,
                                        final String gameCode,
                                        final LocalDate date,
                                        final String stadium,
                                        final String homeTeam,
                                        final String awayTeam,
                                        final LocalTime startTime,
                                        final String payload,
                                        final String contentHash, final LocalDateTime now) {
        boolean identityChanged = !Objects.equals(existing.getGameCode(), gameCode)
                || !existing.getDate().equals(date)
                || !existing.getStadium().equals(stadium)
                || !existing.getHomeTeam().equals(homeTeam)
                || !existing.getAwayTeam().equals(awayTeam)
                || !Objects.equals(existing.getStartTime(), startTime);
        if (!identityChanged && existing.getContentHash().equals(contentHash)) {
            log.debug("No change detected: date={}, stadium={}, home={}, away={}",
                    existing.getDate(), existing.getStadium(), existing.getHomeTeam(), existing.getAwayTeam());
            return false;
        }

        existing.update(gameCode, date, stadium, homeTeam, awayTeam, startTime, now, payload, contentHash);
        log.info("Bronze updated: date={}, stadium={}, home={}, away={}",
                existing.getDate(), existing.getStadium(), existing.getHomeTeam(), existing.getAwayTeam());
        return true;
    }

    private boolean createNewByNaturalKey(
            final String gameCode,
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
                gameCode, date, stadium, homeTeam, awayTeam, startTime,
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
