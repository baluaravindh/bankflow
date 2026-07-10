package com.balu.bankflow.exception;

public class UnauthorizedAccountAccessException extends RuntimeException{
    public UnauthorizedAccountAccessException(String message){
        super(message);
    }
}
