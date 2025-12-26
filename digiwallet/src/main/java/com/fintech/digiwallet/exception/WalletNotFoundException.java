package com.fintech.digiwallet.exception;
public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}