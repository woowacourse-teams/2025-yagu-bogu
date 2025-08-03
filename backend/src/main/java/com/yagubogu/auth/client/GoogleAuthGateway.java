package com.yagubogu.auth.client;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.exception.GoogleAuthExceptionHandler;
import com.yagubogu.global.config.GoogleAuthProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(GoogleAuthProperties.class)
@Component
public class GoogleAuthGateway implements AuthGateway {

    private static final String ID_TOKEN = "id_token";

    private final RestClient googleRestClient;
    private final GoogleAuthExceptionHandler googleAuthExceptionHandler;
    private final GoogleAuthProperties googleAuthProperties;

    public GoogleAuthGateway(
            @Qualifier("googleRestClient") final RestClient googleRestClient,
            final GoogleAuthExceptionHandler googleAuthExceptionHandler,
            final GoogleAuthProperties googleAuthProperties
    ) {
        this.googleRestClient = googleRestClient;
        this.googleAuthExceptionHandler = googleAuthExceptionHandler;
        this.googleAuthProperties = googleAuthProperties;
    }

    @Override
    public AuthResponse validateToken(final LoginRequest loginRequest) {
        return googleRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(googleAuthProperties.tokenInfoUri())
                        .queryParam(ID_TOKEN, loginRequest.idToken())
                        .build())
                .retrieve()
                .onStatus(googleAuthExceptionHandler)
                .body(GoogleAuthResponse.class);
    }
}
