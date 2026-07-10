package com.balu.bankflow.exception;

public class FraudSuspectedException extends RuntimeException{
    public FraudSuspectedException(String message){
        super(message);
    }
}
