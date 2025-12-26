package com.fintech.digiwallet.exception;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}