package com.fintech.digiwallet.exception;

public class FraudDetectedException extends RuntimeException {
    public FraudDetectedException(String message) {
        super(message);
    }
}