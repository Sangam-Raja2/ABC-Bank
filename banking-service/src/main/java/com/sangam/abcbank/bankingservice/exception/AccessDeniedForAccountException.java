package com.sangam.abcbank.bankingservice.exception;

public class AccessDeniedForAccountException extends RuntimeException {
    public AccessDeniedForAccountException(String message) {
        super(message);
    }
}
