package com.yagubogu.global;

import com.yagubogu.global.exception.BadGatewayException;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.global.exception.YaguBoguException;
import com.yagubogu.global.exception.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<?> handleUnAuthorizedException(
            final UnAuthorizedException e,
            final HttpServletRequest request
    ) {
        final String accept = request.getHeader(org.springframework.http.HttpHeaders.ACCEPT);
        final boolean isSse = accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);

        log.warn("[UnAuthorizedException] {}", e.getMessage());

        if (isSse) {
            // ⚠️ SSE는 JSON 바디를 쓰면 협상 충돌(406/500) 위험 → 상태코드만
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 일반 요청: JSON 바디 반환
        ExceptionResponse body = new ExceptionResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
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
     * 409 Conflict
     */
    @ExceptionHandler(value = ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleConflictException(final ConflictException e) {
        log.info("[ConflictException]- {}", e.getMessage());

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
    public ExceptionResponse handleRuntimeException(final RuntimeException runtimeException) {
        String message = "Unexpected server error is occurred";
        log.error("[RuntimeException] Unhandled runtime exception", runtimeException);
        log.error("[RuntimeException] Message", runtimeException.getMessage());

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
