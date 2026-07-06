package com.balu.bankflow.exception;

public class DuplicateUserFoundException extends RuntimeException {
    public DuplicateUserFoundException(String message) {
        super(message);
    }
}
