package com.thoughtworks.rslist.exception;

public class RequestNotValidException extends RuntimeException {
    private final String error;

    public RequestNotValidException(String error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error;
    }
}
