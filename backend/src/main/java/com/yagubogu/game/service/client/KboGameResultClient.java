package com.yagubogu.game.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.dto.KboGameResultResponse.KboScoreBoardSummaryResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.exception.KboClientExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class KboGameResultClient {

    private static final String SUCCESS_CODE = "100";
    private static final String KBO_GAME_RESULT_URI = "/Schedule.asmx/GetScoreBoardScroll";

    private final RestClient kboRestClient;
    private final KboClientExceptionHandler kboClientExceptionHandler;

    public KboGameResultResponse fetchGameResult(final Game game) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0");
        param.add("seasonId", Integer.toString(game.getDate().getYear()));
        param.add("gameId", game.getGameCode());

        try {
            String responseBody = kboRestClient.post()
                    .uri(KBO_GAME_RESULT_URI)
                    .body(param)
                    .retrieve()
                    .onStatus(kboClientExceptionHandler)
                    .body(String.class);
            KboGameResultResponse response = parseToKboResultResponse(responseBody);
            validateGameResultResponse(response);

            return response;
        } catch (Exception e) {
            throw new GameSyncException("Failed to fetch game data from Kbo api", e);
        }
    }

    private KboGameResultResponse parseToKboResultResponse(final String responseBody)
            throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(responseBody);

        String code = root.path("code").asText();
        String msg = root.path("msg").asText();

        JsonNode table3 = om.readTree(root.path("table3").asText());
        JsonNode awayRow = table3.path("rows").path(0).path("row");
        JsonNode homeRow = table3.path("rows").path(1).path("row");

        int awayR = awayRow.path(0).path("Text").asInt();
        int awayH = awayRow.path(1).path("Text").asInt();
        int awayE = awayRow.path(2).path("Text").asInt();
        int awayB = awayRow.path(3).path("Text").asInt();

        int homeR = homeRow.path(0).path("Text").asInt();
        int homeH = homeRow.path(1).path("Text").asInt();
        int homeE = homeRow.path(2).path("Text").asInt();
        int homeB = homeRow.path(3).path("Text").asInt();

        return new KboGameResultResponse(
                code,
                msg,
                new KboScoreBoardSummaryResponse(
                        homeR,
                        homeH,
                        homeE,
                        homeB
                ),
                new KboScoreBoardSummaryResponse(
                        awayR,
                        awayH,
                        awayE,
                        awayB
                )
        );
    }

    private void validateGameResultResponse(final KboGameResultResponse response) {
        if (isResponseErrorCode(response.statusCode())) {
            throw new GameSyncException("Unexpected response code from Kbo api: " + response.msg());
        }
    }

    private boolean isResponseErrorCode(final String statusCode) {
        return !SUCCESS_CODE.equals(statusCode);
    }
}
