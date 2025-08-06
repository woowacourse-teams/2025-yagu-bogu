package com.yagubogu.game.exception;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class KboClientExceptionHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is5xxServerError() ||
                response.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(
            final URI url,
            final HttpMethod method,
            final ClientHttpResponse response
    ) throws IOException {
        throw new GameSyncException("Kbo server error: " + response.getStatusCode());
    }
}
