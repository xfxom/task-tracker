package com.track.task.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super("Forbidden exception: " + message);
    }

    public ForbiddenException() {
        super("Forbidden exception");
    }
}
