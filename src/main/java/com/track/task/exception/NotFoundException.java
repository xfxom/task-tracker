package com.track.task.exception;

import org.springframework.security.core.AuthenticationException;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NotFoundException(String msg) {
        super(msg);
    }
}
