package com.yagubogu.game.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.dto.KboClientResponse;
import com.yagubogu.global.exception.KboClientException;
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

    private final RestClient kboRestClient;
    private final ObjectMapper objectMapper;

    public KboClientResponse fetchGame(LocalDate date) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("leId", "1");
        param.add("srId", "0,1,3,4,5,6,7,8,9");
        param.add("date", date.format(DateTimeFormatter.BASIC_ISO_DATE));

        try {
            String responseBody = kboRestClient.post()
                    .uri("/GetKboGameList")
                    .body(param)
                    .retrieve()
                    .body(String.class);
            KboClientResponse kboClientResponse = objectMapper.readValue(responseBody, KboClientResponse.class);
            validateResponse(kboClientResponse);

            return kboClientResponse;
        } catch (Exception e) {
            throw new KboClientException("Failed to fetch game data from Kbo api");
        }
    }

    private void validateResponse(final KboClientResponse kboClientResponse) {
        if (isResponseErrorCode(kboClientResponse)) {
            throw new KboClientException("Unexpected response code from Kbo api");
        }
    }

    private boolean isResponseErrorCode(final KboClientResponse kboClientResponse) {
        return !kboClientResponse.code().equals("100");
    }
}
