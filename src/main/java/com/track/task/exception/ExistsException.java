package com.track.task.exception;

public class ExistsException extends RuntimeException {
    public ExistsException(String message) {
        super(message + " exists exception");
    }

    public ExistsException() {
        super("Entity exists exception");
    }
}
