package com.track.task.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException caught: {}", ex.getMessage(), ex);
        return Map.of("error", ex.getMessage());
    }
    @ExceptionHandler(EmptyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleEmptyException(EmptyException ex) {
        log.error("EmptyException caught: {}", ex.getMessage(), ex);

        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(ExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Map<String, String> handleExistsException(ExistsException ex) {
        log.error("ExistsException caught: {}", ex.getMessage(), ex);
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Map<String, String> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("BadCredentialsException caught: {}", ex.getMessage(), ex);
        return Map.of("error", ex.getMessage());
    }

}