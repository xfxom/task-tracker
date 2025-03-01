package com.track.task.exception;

public class EmptyException extends RuntimeException {
    public EmptyException(String message) {
        super("Entity empty: " + message);
    }

    public EmptyException() {
        super("This entity is empty");
    }
}
