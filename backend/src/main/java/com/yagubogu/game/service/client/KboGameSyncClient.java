package com.yagubogu.game.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.exception.KboClientExceptionHandler;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KboGameSyncClient {

    private static final String SUCCESS_CODE = "100";
    private static final String KBO_GAMES_URI = "/Main.asmx/GetKboGameList";

    private final RestClient kboRestClient;
    private final ObjectMapper objectMapper;
    private final KboClientExceptionHandler kboClientExceptionHandler;

    public KboGameSyncClient(
            @Qualifier("kboRestClient") final RestClient kboRestClient,
            final ObjectMapper objectMapper,
            final KboClientExceptionHandler kboClientExceptionHandler
    ) {
        this.kboRestClient = kboRestClient;
        this.objectMapper = objectMapper;
        this.kboClientExceptionHandler = kboClientExceptionHandler;
    }

    public KboGamesResponse fetchGames(final LocalDate date) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0,1,3,4,5,6,7,8,9");
        param.add("date", date.format(DateTimeFormatter.BASIC_ISO_DATE));

        try {
            String responseBody = kboRestClient.post()
                    .uri(KBO_GAMES_URI)
                    .body(param)
                    .retrieve()
                    .onStatus(kboClientExceptionHandler)
                    .body(String.class);
            KboGamesResponse kboGamesResponse = objectMapper.readValue(
                    responseBody,
                    KboGamesResponse.class
            );
            validateGameScheduleResponse(kboGamesResponse);

            return kboGamesResponse;
        } catch (Exception e) {
            throw new GameSyncException("Failed to fetch game data from Kbo api", e);
        }
    }

    private void validateGameScheduleResponse(final KboGamesResponse response) {
        if (isResponseErrorCode(response.statusCode())) {
            throw new GameSyncException("Unexpected response code from Kbo api: " + response.msg());
        }
    }

    private boolean isResponseErrorCode(final String statusCode) {
        return !SUCCESS_CODE.equals(statusCode);
    }
}
