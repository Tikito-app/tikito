package org.tikito.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class TikitoControllerAdvice {

    @ExceptionHandler(value = {ClientValidationException.class})
    protected ResponseEntity<Object> handleClientValidationException(final Exception ex, final WebRequest request) {
        return handleExceptionInternal(ex, new ServerResponse<>(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex, final WebRequest request) {
        return handleExceptionInternal(ex, new ServerResponse<>(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, @Nullable final Object body, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        return new ResponseEntity<>(body, headers, status);
    }
}
