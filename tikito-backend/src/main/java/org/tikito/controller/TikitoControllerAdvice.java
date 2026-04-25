package org.tikito.controller;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.tikito.exception.ClientFormExceptionResponse;
import org.tikito.exception.ClientValidationException;
import org.tikito.exception.InvalidCredentialsException;
import org.tikito.exception.ClientFormExceptionResponse.ClientFormValidationExceptionField;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class TikitoControllerAdvice {

    @ExceptionHandler(value = {ClientValidationException.class})
    protected ResponseEntity<Object> handleClientValidationException(final ClientValidationException ex) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, new ServerResponse<>(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    protected ResponseEntity<Object> handleNoSuchElementException(final NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, new ServerResponse<>(ex), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServerResponse<ClientFormExceptionResponse>> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        final List<ClientFormValidationExceptionField> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(new ClientFormValidationExceptionField(error.getField(), error.getDefaultMessage())));

        final ClientFormExceptionResponse errorResponse = new ClientFormExceptionResponse(
                fieldErrors
        );

        return new ResponseEntity<>(new ServerResponse<>(ClientFormExceptionResponse.class.getSimpleName(), errorResponse), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InvalidCredentialsException.class})
    protected ResponseEntity<Object> handleInvalidCredentialsException(final InvalidCredentialsException ex) {
        log.info(ex.getMessage(), ex);
        return handleExceptionInternal(ex, new ServerResponse<>(ex), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, @Nullable final Object body, final HttpHeaders headers, final HttpStatus status) {
        log.warn(ex.getMessage(), ex);
        return new ResponseEntity<>(body, headers, status);
    }
}
