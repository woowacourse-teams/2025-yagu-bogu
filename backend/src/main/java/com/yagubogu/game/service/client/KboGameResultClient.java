package com.yagubogu.game.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.exception.KboClientExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KboGameResultClient {

    private static final String SUCCESS_CODE = "100";
    private static final String KBO_GAME_RESULT_URI = "/Schedule.asmx/GetScoreBoardScroll";
    private static final String KBO_GAME_PITCHERS_URI = "/Schedule.asmx/GetBoxScoreScroll";

    private final RestClient kboRestClient;
    private final ObjectMapper objectMapper;
    private final KboClientExceptionHandler kboClientExceptionHandler;

    public KboGameResultClient(
            @Qualifier("kboRestClient") final RestClient kboRestClient,
            final ObjectMapper objectMapper,
            final KboClientExceptionHandler kboClientExceptionHandler
    ) {
        this.kboRestClient = kboRestClient;
        this.objectMapper = objectMapper;
        this.kboClientExceptionHandler = kboClientExceptionHandler;
    }

    public KboGameResultResponse fetchGameResult(final Game game) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("leId", "1");
        params.add("srId", "0");
        params.add("seasonId", Integer.toString(game.getDate().getYear()));
        params.add("gameId", game.getGameCode());

        try {
            // 1. 스코어보드 정보 조회 및 파싱 (code, msg 포함)
            ScoreBoardDataWithStatus scoreBoards = parseScoreBoard(callKboApi(KBO_GAME_RESULT_URI, params));

            // 2. 투수 정보(박스스코어) 조회 및 파싱 (code, msg 포함)
            PitcherDataWithStatus pitchers = parsePitchers(callKboApi(KBO_GAME_PITCHERS_URI, params));

            // 3. 코드/메시지 확인 (한쪽이라도 실패 시 예외)
            if (!SUCCESS_CODE.equals(scoreBoards.code())) {
                throw new GameSyncException("ScoreBoard API error: " + scoreBoards.msg());
            }
            if (!SUCCESS_CODE.equals(pitchers.code())) {
                throw new GameSyncException("Pitchers API error: " + pitchers.msg());
            }

            // 4. 결과 조합하여 최종 DTO 생성
            return new KboGameResultResponse(
                    scoreBoards.homeScoreBoard(),
                    scoreBoards.awayScoreBoard(),
                    pitchers.homePitcher(),
                    pitchers.awayPitcher()
            );
        } catch (Exception e) {
            throw new GameSyncException("Failed to fetch game data from Kbo api", e);
        }
    }

    private ScoreBoardDataWithStatus parseScoreBoard(final String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);

        String code = root.path("code").asText();
        String msg = root.path("msg").asText();
        ScoreBoard homeScoreBoard = null;
        ScoreBoard awayScoreBoard = null;

        if (SUCCESS_CODE.equals(code)) {
            JsonNode table2 = objectMapper.readTree(root.path("table2").asText());
            JsonNode awayInningRow = table2.path("rows").path(0).path("row");
            JsonNode homeInningRow = table2.path("rows").path(1).path("row");

            List<String> awayInnings = new ArrayList<>();
            for (JsonNode inningScore : awayInningRow) {
                awayInnings.add(inningScore.path("Text").asText("-"));
            }

            List<String> homeInnings = new ArrayList<>();
            for (JsonNode inningScore : homeInningRow) {
                homeInnings.add(inningScore.path("Text").asText("-"));
            }

            JsonNode table3 = objectMapper.readTree(root.path("table3").asText());
            JsonNode awayRow = table3.path("rows").path(0).path("row");
            JsonNode homeRow = table3.path("rows").path(1).path("row");

            int awayR = awayRow.path(0).path("Text").asInt(0);
            int awayH = awayRow.path(1).path("Text").asInt(0);
            int awayE = awayRow.path(2).path("Text").asInt(0);
            int awayB = awayRow.path(3).path("Text").asInt(0);

            int homeR = homeRow.path(0).path("Text").asInt(0);
            int homeH = homeRow.path(1).path("Text").asInt(0);
            int homeE = homeRow.path(2).path("Text").asInt(0);
            int homeB = homeRow.path(3).path("Text").asInt(0);

            awayScoreBoard = new ScoreBoard(awayR, awayH, awayE, awayB, awayInnings);
            homeScoreBoard = new ScoreBoard(homeR, homeH, homeE, homeB, homeInnings);
        }

        return new ScoreBoardDataWithStatus(code, msg, homeScoreBoard, awayScoreBoard);
    }

    private record ScoreBoardDataWithStatus(
            String code,
            String msg,
            ScoreBoard homeScoreBoard,
            ScoreBoard awayScoreBoard
    ) {
    }

    private PitcherDataWithStatus parsePitchers(final String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);

        String code = root.path("code").asText();
        String msg = root.path("msg").asText();
        String homePitcher = null;
        String awayPitcher = null;

        if (SUCCESS_CODE.equals(code)) {
            JsonNode arrPitcher = root.path("arrPitcher");
            if (arrPitcher.isArray()) {
                for (int i = 0; i < arrPitcher.size(); i++) {
                    JsonNode teamPitcherNode = arrPitcher.get(i);
                    String tableJsonString = teamPitcherNode.path("table").asText();

                    if (tableJsonString.isEmpty()) {
                        continue;
                    }

                    JsonNode pitcherTable = objectMapper.readTree(tableJsonString);
                    for (JsonNode pitcherRow : pitcherTable.path("rows")) {
                        JsonNode columns = pitcherRow.path("row");
                        if (!columns.isArray() || columns.size() < 3) {
                            continue;
                        }

                        String name = columns.path(0).path("Text").asText();
                        String result = columns.path(2).path("Text").asText();

                        // 승/패/무 결과에 해당하면
                        if ("승".equals(result) || "패".equals(result) || "무".equals(result)) {
                            if (i == 0) {
                                awayPitcher = name;
                            } else if (i == 1) {
                                homePitcher = name;
                            }
                            break;
                        }
                    }
                }
            }
        }

        return new PitcherDataWithStatus(code, msg, homePitcher, awayPitcher);
    }

    private record PitcherDataWithStatus(
            String code,
            String msg,
            String homePitcher,
            String awayPitcher
    ) {
    }

    private String callKboApi(String uri, MultiValueMap<String, String> params) {
        return kboRestClient.post()
                .uri(uri)
                .body(params)
                .retrieve()
                .onStatus(kboClientExceptionHandler)
                .body(String.class);
    }
}
