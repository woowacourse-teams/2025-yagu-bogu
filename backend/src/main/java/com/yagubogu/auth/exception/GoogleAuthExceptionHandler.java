package com.yagubogu.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.auth.dto.GoogleErrorResponse;
import com.yagubogu.global.exception.BadGatewayException;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class GoogleAuthExceptionHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    public GoogleAuthExceptionHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is5xxServerError() ||
                response.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(final URI url, final HttpMethod method, final ClientHttpResponse response)
            throws IOException {
        GoogleErrorResponse errorResponse = parseErrorResponse(response);
        String error = errorResponse.error();
        throw new BadGatewayException(error);
    }

    private GoogleErrorResponse parseErrorResponse(ClientHttpResponse response) throws IOException {
        return objectMapper.readValue(response.getBody(), GoogleErrorResponse.class);
    }
}
