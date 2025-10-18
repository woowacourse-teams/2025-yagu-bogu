package yagubogu.crawling.game.service.client;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import yagubogu.crawling.game.dto.KboGamesParam;
import com.yagubogu.game.exception.GameSyncException;
import yagubogu.crawling.game.exception.KboClientExceptionHandler;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class KboGameSyncClient {

    private static final String SUCCESS_CODE = "100";
    private static final String KBO_GAMES_URI = "/Main.asmx/GetKboGameList";

    private final RestClient kboRestClient;
    private final ObjectMapper objectMapper;
    private final KboClientExceptionHandler kboClientExceptionHandler;

    public KboGamesParam fetchGames(final LocalDate date) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0,1,3,4,5,6,7,8,9");
        param.add("date", date.format(BASIC_ISO_DATE));

        try {
            String responseBody = kboRestClient.post()
                    .uri(KBO_GAMES_URI)
                    .body(param)
                    .retrieve()
                    .onStatus(kboClientExceptionHandler)
                    .body(String.class);
            KboGamesParam KboGamesParam = objectMapper.readValue(
                    responseBody,
                    KboGamesParam.class
            );
            validateGameScheduleResponse(KboGamesParam);

            return KboGamesParam;
        } catch (Exception e) {
            throw new GameSyncException("Failed to fetch game data from Kbo api", e);
        }
    }

    private void validateGameScheduleResponse(final KboGamesParam response) {
        if (isResponseErrorCode(response.statusCode())) {
            throw new GameSyncException("Unexpected response code from Kbo api: " + response.msg());
        }
    }

    private boolean isResponseErrorCode(final String statusCode) {
        return !SUCCESS_CODE.equals(statusCode);
    }
}
