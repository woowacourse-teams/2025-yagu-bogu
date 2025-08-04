package com.yagubogu.game.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.KboGameListResponse;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.dto.KboGameResultResponse.KboScoreBoardResponse;
import com.yagubogu.global.exception.ClientException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class KboClient {

    public static final String SUCCESS_CODE = "100";

    private final RestClient kboRestClient;
    private final ObjectMapper objectMapper;

    public KboGameListResponse fetchGameList(final LocalDate date) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0,1,3,4,5,6,7,8,9");
        param.add("date", date.format(DateTimeFormatter.BASIC_ISO_DATE));

        try {
            String responseBody = kboRestClient.post()
                    .uri("/Main.asmx/GetKboGameList")
                    .body(param)
                    .retrieve()
                    .body(String.class);
            KboGameListResponse kboGameListResponse = objectMapper.readValue(
                    responseBody,
                    KboGameListResponse.class
            );
            validateGameScheduleResponse(kboGameListResponse);

            return kboGameListResponse;
        } catch (Exception e) {
            throw new ClientException("Failed to fetch game data from Kbo api", e);
        }
    }

    public KboGameResultResponse fetchGameResult(final Game game) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0");
        param.add("seasonId", Integer.toString(game.getDate().getYear()));
        param.add("gameId", game.getGameCode());

        try {
            String responseBody = kboRestClient.post()
                    .uri("/Schedule.asmx/GetScoreBoardScroll")
                    .body(param)
                    .retrieve()
                    .body(String.class);
            KboGameResultResponse response = parseResultResponseToKboResultResponse(responseBody);
            validateGameResultResponse(response);

            return response;
        } catch (Exception e) {
            throw new ClientException("Failed to fetch game data from Kbo api", e);
        }
    }

    private KboGameResultResponse parseResultResponseToKboResultResponse(final String responseBody)
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
                new KboScoreBoardResponse(
                        homeR,
                        homeH,
                        homeE,
                        homeB
                ),
                new KboScoreBoardResponse(
                        awayR,
                        awayH,
                        awayE,
                        awayB
                )
        );
    }

    private void validateGameScheduleResponse(final KboGameListResponse response) {
        if (isResponseErrorCode(response.statusCode())) {
            throw new ClientException("Unexpected response code from Kbo api: " + response.msg());
        }
    }

    private void validateGameResultResponse(final KboGameResultResponse response) {
        if (isResponseErrorCode(response.statusCode())) {
            throw new ClientException("Unexpected response code from Kbo api: " + response.msg());
        }
    }

    private boolean isResponseErrorCode(final String statusCode) {
        return !SUCCESS_CODE.equals(statusCode);
    }
}
