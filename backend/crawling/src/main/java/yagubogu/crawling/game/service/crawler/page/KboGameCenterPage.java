package yagubogu.crawling.game.service.crawler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Slf4j
public class KboGameCenterPage extends BaseKboPage {

    public KboGameCenterPage(Page page, KboCrawlerProperties properties) {
        super(page, properties);
    }

    @Override
    protected String getBaseUrl() {
        return properties.getCrawler().getGameCenterUrl();
    }

    @Override
    protected boolean needsDateChangeValidation() {
        return false; // GameCenter는 날짜 라벨 확인 불필요
    }

    @Override
    protected void waitForContentUpdate(long timeout) {
        // 경기 목록 대기 (없을 수도 있음)
        try {
            page.waitForSelector(".game-list-n > li",
                    new Page.WaitForSelectorOptions()
                            .setTimeout(1500)
                            .setState(WaitForSelectorState.ATTACHED)
            );
        } catch (TimeoutError e) {
            log.info("경기 목록 없음 (경기 없는 날)");
        }
    }

    // ==================== 데이터 추출 ====================

    /**
     * 현재 날짜 텍스트 추출
     */
    public String getDateText() {
        String dateText = page.locator("#lblGameDate").textContent();
        return dateText.substring(0, 10).replace(".", "-");
    }

    /**
     * 경기 목록 Locator 반환
     */
    public Locator getGameList() {
        return page.locator(".game-list-n > li.game-cont");
    }

    /**
     * 경기 개수 반환
     */
    public int getGameCount() {
        return getGameList().count();
    }

    /**
     * 특정 인덱스의 경기 요소 반환
     */
    public Locator getGameElement(int index) {
        return getGameList().nth(index);
    }

    // ==================== 경기 상세 정보 추출 ====================

    /**
     * game-cont 요소에서 경기 상세 정보 추출
     */
    public GameCenterDetail extractGameDetail(Locator gameElement, String date) {
        try {
            GameCenterDetail gameCenter = new GameCenterDetail();
            gameCenter.setDate(date);

            // 기본 속성
            extractBasicAttributes(gameElement, gameCenter);

            // 경기 상태
            extractGameStatus(gameElement, gameCenter);

            // Top 영역 (경기장, 날씨, 시간)
            extractTopSection(gameElement, gameCenter);

            // Middle 영역 (중계, 상태)
            extractMiddleSection(gameElement, gameCenter);

            // 팀 정보 (점수, 투수)
            extractTeamInfo(gameElement, gameCenter);

            log.debug("경기 정보 수집 완료: {} vs {} ({})",
                    gameCenter.getAwayTeamName(), gameCenter.getHomeTeamName(), gameCenter.getStatus());

            return gameCenter;

        } catch (Exception e) {
            log.error("경기 정보 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 내부 추출 메서드 ====================

    private void extractBasicAttributes(Locator gameElement, GameCenterDetail gameCenter) {
        gameCenter.setGameCode(gameElement.getAttribute("g_id"));
        gameCenter.setGameDate(gameElement.getAttribute("g_dt"));
        gameCenter.setGameSc(gameElement.getAttribute("game_sc"));
        gameCenter.setAwayTeamCode(gameElement.getAttribute("away_id"));
        gameCenter.setHomeTeamCode(gameElement.getAttribute("home_id"));
        gameCenter.setAwayTeamName(gameElement.getAttribute("away_nm"));
        gameCenter.setHomeTeamName(gameElement.getAttribute("home_nm"));
        gameCenter.setStadium(gameElement.getAttribute("s_nm"));
    }

    private void extractGameStatus(Locator gameElement, GameCenterDetail gameCenter) {
        String classAttr = gameElement.getAttribute("class");
        if (classAttr == null) {
            return;
        }

        if (classAttr.contains("end")) {
            gameCenter.setGameStatus("경기종료");
        } else if (classAttr.contains("cancel")) {
            gameCenter.setGameStatus("경기취소");
        } else if (classAttr.contains("ing")) {
            gameCenter.setGameStatus("경기중");
        } else {
            gameCenter.setGameStatus("경기예정");
        }
    }

    private void extractTopSection(Locator gameElement, GameCenterDetail gameCenter) {
        Locator topItems = gameElement.locator(".top > ul > li");
        int topCount = topItems.count();

        if (topCount >= 1) {
            gameCenter.setStadiumName(topItems.nth(0).textContent().trim());
        }

        // 날씨 이미지
        if (topCount >= 2) {
            Locator weatherImg = topItems.nth(1).locator("img");
            if (weatherImg.count() > 0) {
                gameCenter.setWeatherIcon(weatherImg.getAttribute("src"));
            }
        }

        // 경기 시간
        if (topCount >= 3) {
            gameCenter.setStartTime(topItems.nth(topCount - 1).textContent().trim());
        } else if (topCount == 2) {
            gameCenter.setStartTime(topItems.nth(1).textContent().trim());
        }
    }

    private void extractMiddleSection(Locator gameElement, GameCenterDetail gameCenter) {
        Locator broadcastingElem = gameElement.locator(".middle .broadcasting");
        if (broadcastingElem.count() > 0) {
            gameCenter.setBroadcasting(broadcastingElem.textContent().trim());
        }

        Locator statusElem = gameElement.locator(".middle .staus");
        if (statusElem.count() > 0) {
            gameCenter.setStatus(statusElem.textContent().trim());
        }
    }

    private void extractTeamInfo(Locator gameElement, GameCenterDetail gameCenter) {
        boolean isTopInning = isTopInning(gameCenter.getStatus());
        extractAwayTeamInfo(gameElement, gameCenter, isTopInning);
        extractHomeTeamInfo(gameElement, gameCenter, isTopInning);
    }

    /**
     * 이닝 초(원정팀 공격)/말(홈팀 공격) 여부.
     * .today-pitcher는 공격중인 팀에서는 타자, 수비중인 팀에서는 투수를 나타내므로
     * 어느 팀에 어떤 의미를 부여할지 이걸로 판별한다.
     */
    private boolean isTopInning(String status) {
        return status != null && status.contains("초");
    }

    private void extractAwayTeamInfo(Locator gameElement, GameCenterDetail gameCenter, boolean isTopInning) {
        Locator awayTeam = gameElement.locator(".team.away");
        if (awayTeam.count() == 0) {
            return;
        }

        // 점수
        Locator awayScore = awayTeam.locator(".score");
        if (awayScore.count() > 0) {
            gameCenter.setAwayScore(awayScore.textContent().trim());

            String scoreClass = awayScore.getAttribute("class");
            if (scoreClass != null && scoreClass.contains("win")) {
                gameCenter.setWinner("away");
            }
        }

        // 현재 타자/투수: 초(원정팀 공격)면 타자, 아니면 투수
        String todayPlayer = extractTodayPlayer(awayTeam);
        if (todayPlayer != null) {
            if (isTopInning) {
                gameCenter.setCurrentBatterTeam("away");
                gameCenter.setCurrentBatterName(todayPlayer);
            } else {
                gameCenter.setCurrentPitcherTeam("away");
                gameCenter.setCurrentPitcherName(todayPlayer);
            }
        }
    }

    private void extractHomeTeamInfo(Locator gameElement, GameCenterDetail gameCenter, boolean isTopInning) {
        Locator homeTeam = gameElement.locator(".team.home");
        if (homeTeam.count() == 0) {
            return;
        }

        // 점수
        Locator homeScore = homeTeam.locator(".score");
        if (homeScore.count() > 0) {
            gameCenter.setHomeScore(homeScore.textContent().trim());

            String scoreClass = homeScore.getAttribute("class");
            if (scoreClass != null && scoreClass.contains("win")) {
                gameCenter.setWinner("home");
            }
        }

        // 현재 타자/투수: 말(홈팀 공격)이면 타자, 아니면 투수
        String todayPlayer = extractTodayPlayer(homeTeam);
        if (todayPlayer != null) {
            if (!isTopInning) {
                gameCenter.setCurrentBatterTeam("home");
                gameCenter.setCurrentBatterName(todayPlayer);
            } else {
                gameCenter.setCurrentPitcherTeam("home");
                gameCenter.setCurrentPitcherName(todayPlayer);
            }
        }
    }

    private String extractTodayPlayer(Locator teamElement) {
        Locator pitcherElem = teamElement.locator(".today-pitcher");
        if (pitcherElem.count() == 0) {
            return null;
        }

        String text = pitcherElem.textContent().trim();
        return text.isEmpty() ? null : text;
    }
}
