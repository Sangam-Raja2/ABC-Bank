package com.sangam.abcbank.loanservice.exception;

public class AccessDeniedForAccountException extends RuntimeException {
    public AccessDeniedForAccountException(String message) {
        super(message);
    }
}
