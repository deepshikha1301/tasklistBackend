package com.api.tasklist.exception;

public class NoSuchUserException extends RuntimeException{

    private String message;

    public NoSuchUserException(String message) {
        super(message);
        this.message = message;
    }
}
