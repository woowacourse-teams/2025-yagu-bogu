package com.yagubogu.global;

import com.yagubogu.global.exception.BadGatewayException;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.global.exception.YaguBoguException;
import com.yagubogu.global.exception.dto.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 Bad Request
     */
    @ExceptionHandler(value = BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(final BadRequestException e) {
        log.info("[BadRequestException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 401 UnAuthorized
     */
    @ExceptionHandler(value = UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleUnAuthorizedException(final UnAuthorizedException e) {
        log.warn("[UnAuthorizedException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 403 Forbidden
     */
    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleForbiddenException(final ForbiddenException e) {
        log.warn("[ForbiddenException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 404 Not Found
     */
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFoundException(final NotFoundException e) {
        log.info("[NotFoundException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 422 UnprocessableEntity
     */
    @ExceptionHandler(value = UnprocessableEntityException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ExceptionResponse handleUnprocessableException(final UnprocessableEntityException e) {
        log.info("[UnprocessableEntityException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 500 Internal Server Error
     */
    @ExceptionHandler(YaguBoguException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleYaguBoguException(final YaguBoguException e) {
        log.error("[YaguBoguException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleRuntimeException() {
        String message = "Unexpected server error is occurred";
        log.error("[RuntimeException]- {}", message);

        return new ExceptionResponse(message);
    }

    /**
     * 502 Bad Gateway Exception
     */
    @ExceptionHandler(BadGatewayException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ExceptionResponse handleBadGatewayException(final BadGatewayException e) {
        log.warn("[BadGatewayException]- {}", e.getMessage());

        return new ExceptionResponse(e.getMessage());
    }
}
