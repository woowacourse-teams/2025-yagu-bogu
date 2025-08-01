package com.yagubogu.auth.client;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.exception.GoogleAuthExceptionHandler;
import com.yagubogu.global.config.GoogleAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@EnableConfigurationProperties(GoogleAuthProperties.class)
public class GoogleAuthGateway implements AuthGateway {

    private static final String ID_TOKEN = "id_token";

    private final RestClient restClient;
    private final GoogleAuthExceptionHandler googleAuthExceptionHandler;
    private final GoogleAuthProperties googleAuthProperties;

    public GoogleAuthGateway(final RestClient restClient,
                             final GoogleAuthExceptionHandler googleAuthExceptionHandler,
                             final GoogleAuthProperties googleAuthProperties) {
        this.restClient = restClient;
        this.googleAuthExceptionHandler = googleAuthExceptionHandler;
        this.googleAuthProperties = googleAuthProperties;
    }

    @Override
    public AuthResponse validateToken(final LoginRequest loginRequest) {
        return restClient.get()
                .uri(googleAuthProperties.getTokenInfoUri(), ID_TOKEN, loginRequest.idToken())
                .retrieve()
                .onStatus(googleAuthExceptionHandler)
                .body(AuthResponse.class);
    }
}
