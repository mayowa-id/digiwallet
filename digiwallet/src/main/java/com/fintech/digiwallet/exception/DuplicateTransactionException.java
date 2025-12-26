package com.fintech.digiwallet.exception;
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}