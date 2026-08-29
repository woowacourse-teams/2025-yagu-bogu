package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCenterSyncService {

    private final KboGameCenterCrawler crawler;
    private final BronzeGameService bronzeGameService;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    /**
     * 특정 날짜 경기 상세 정보 수집 및 Bronze Layer 저장
     */
    public int fetchGameCenter(LocalDate date) {
        GameCenter dailyData = fetchGameCenterOnly(date);

        return saveToBronzeLayer(dailyData.getGames());
    }

    public GameCenter fetchGameCenterOnly(LocalDate date) {
        return crawler.fetchDailyGameCenter(date);
    }

    /**
     * GameCenterDetail 리스트를 받아서 Bronze Layer에 저장
     */
    @Transactional
    public int saveToBronzeLayer(java.util.List<GameCenterDetail> gameDetails) {
        int updatedCount = 0;

        for (GameCenterDetail detail : gameDetails) {
            try {
                boolean updated = updateGameState(detail);
                if (updated) {
                    updatedCount++;
                }

                // 현재 타자/투수는 재처리 대상이 아니라 games 테이블에 바로 반영
                updateLiveBatterAndPitcher(detail);

                // 선발 예고 투수는 재처리 대상이 아니라 games 테이블에 바로 반영
                updateProbablePitchers(detail);
            } catch (Exception e) {
                log.error("[BRONZE] 경기 상태 저장 실패: gameCode={}", detail.getGameCode(), e);
            }
        }

        log.info("[BRONZE] Processed {} games, {} data updates", gameDetails.size(), updatedCount);
        return updatedCount;
    }

    /**
     * 크롤링한 현재 타자/투수를 games 테이블에 직접 반영 (Bronze/ETL을 거치지 않음)
     */
    private void updateLiveBatterAndPitcher(GameCenterDetail detail) {
        if (detail.getCurrentBatterTeam() == null
                && detail.getCurrentBatterName() == null
                && detail.getCurrentPitcherTeam() == null
                && detail.getCurrentPitcherName() == null) {
            return;
        }

        LocalDate date = parseDate(detail.getGameDate());
        String stadiumLocation = detail.getStadiumName();
        String homeTeamName = detail.getHomeTeamName();
        String awayTeamName = detail.getAwayTeamName();
        LocalTime startTime = parseTime(detail.getStartTime());

        Team homeTeam = teamRepository.findByShortName(homeTeamName).orElse(null);
        Team awayTeam = teamRepository.findByShortName(awayTeamName).orElse(null);
        Stadium stadium = stadiumRepository.findByLocation(stadiumLocation).orElse(null);

        if (homeTeam == null || awayTeam == null || stadium == null) {
            log.debug("[LIVE_STATE] Team/Stadium not found, skip batter/pitcher update: stadium={}, home={}, away={}",
                    stadiumLocation, homeTeamName, awayTeamName);
            return;
        }

        gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(date, stadium, homeTeam, awayTeam,
                        startTime)
                .ifPresentOrElse(
                        game -> game.updateLiveBatterAndPitcher(
                                detail.getCurrentBatterTeam(),
                                detail.getCurrentBatterName(),
                                detail.getCurrentPitcherTeam(),
                                detail.getCurrentPitcherName()
                        ),
                        () -> log.debug("[LIVE_STATE] Game not found, skip batter/pitcher update: gameCode={}",
                                detail.getGameCode())
                );
    }

    /**
     * 크롤링한 선발 예고 투수를 games 테이블에 직접 반영 (Bronze/ETL을 거치지 않음).
     * 예정 경기에서만 값이 채워지며, 둘 다 없으면 건드리지 않는다.
     */
    private void updateProbablePitchers(GameCenterDetail detail) {
        if (detail.getHomeProbablePitcher() == null && detail.getAwayProbablePitcher() == null) {
            return;
        }

        LocalDate date = parseDate(detail.getGameDate());
        String stadiumLocation = detail.getStadiumName();
        String homeTeamName = detail.getHomeTeamName();
        String awayTeamName = detail.getAwayTeamName();
        LocalTime startTime = parseTime(detail.getStartTime());

        Team homeTeam = teamRepository.findByShortName(homeTeamName).orElse(null);
        Team awayTeam = teamRepository.findByShortName(awayTeamName).orElse(null);
        Stadium stadium = stadiumRepository.findByLocation(stadiumLocation).orElse(null);

        if (homeTeam == null || awayTeam == null || stadium == null) {
            log.debug("[PROBABLE_PITCHER] Team/Stadium not found, skip: stadium={}, home={}, away={}",
                    stadiumLocation, homeTeamName, awayTeamName);
            return;
        }

        gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(date, stadium, homeTeam, awayTeam,
                        startTime)
                .ifPresentOrElse(
                        game -> game.updateProbablePitchers(
                                detail.getHomeProbablePitcher(), detail.getAwayProbablePitcher()
                        ),
                        () -> log.debug("[PROBABLE_PITCHER] Game not found, skip: gameCode={}",
                                detail.getGameCode())
                );
    }

    /**
     * 스코어보드(ScoreBoard.aspx) 크롤링 결과로 진루정보/볼·스트라이크·아웃을 games 테이블에 직접 반영
     * (Bronze/ETL을 거치지 않음)
     */
    public void updateLiveBaseState(
            String gameCode,
            Boolean firstBaseOccupied,
            Boolean secondBaseOccupied,
            Boolean thirdBaseOccupied,
            Integer balls,
            Integer strikes,
            Integer outs
    ) {
        gameRepository.findByGameCode(gameCode).ifPresentOrElse(
                game -> game.updateLiveBaseState(
                        firstBaseOccupied, secondBaseOccupied, thirdBaseOccupied, balls, strikes, outs
                ),
                () -> log.debug("[LIVE_STATE] Game not found for gameCode={}, skip base state update", gameCode)
        );
    }

    /**
     * 개별 경기 상태를 Bronze Layer에 반영
     */
    private boolean updateGameState(GameCenterDetail detail) {
        LocalDate date = parseDate(detail.getGameDate());
        String stadium = detail.getStadiumName();
        String homeTeam = detail.getHomeTeamName();
        String awayTeam = detail.getAwayTeamName();
        LocalTime startTime = parseTime(detail.getStartTime());
        GameState state = GameState.tryFromName(detail.getStatus()).orElse(null);
        if (state == null) {
            log.warn("[BRONZE] 알 수 없는 GameCenter 경기 상태로 저장 생략: gameCode={}, status={}",
                    detail.getGameCode(), detail.getStatus());
            return false;
        }

        boolean updated = bronzeGameService.updateGameState(
                detail.getGameCode(), date, stadium, homeTeam, awayTeam, startTime, state
        );

        if (updated) {
            log.debug("[BRONZE] Game state synced: gameCode={}, state={}",
                    detail.getGameCode(), state);
        }

        return updated;
    }

    /**
     * 날짜 파싱: "20251021" → LocalDate
     */
    private LocalDate parseDate(String gameDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(gameDate, formatter);
        } catch (Exception e) {
            throw new GameSyncException("Invalid date format: " + gameDate);
        }
    }

    /**
     * 시간 파싱: "18:30" → LocalTime
     */
    private LocalTime parseTime(String startTime) {
        try {
            return LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new GameSyncException("Invalid time format: " + startTime);
        }
    }
}
