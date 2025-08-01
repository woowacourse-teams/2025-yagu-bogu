package com.yagubogu.global;

import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.YaguBoguException;
import com.yagubogu.global.exception.dto.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 403 Forbidden
     */
    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleForbiddenException(final ForbiddenException e) {
        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 404 Not Found
     */
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFoundException(final NotFoundException e) {
        return new ExceptionResponse(e.getMessage());
    }

    /**
     * 500 Internal Server Error
     */
    @ExceptionHandler(YaguBoguException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleYaguBoguException(final YaguBoguException e) {
        return new ExceptionResponse(e.getMessage());
    }
}
