package com.sangam.abcbank.emiservice.exception;

public class TransitionNotAllowedException extends RuntimeException {
    public TransitionNotAllowedException(String message) {
        super(message);
    }
}
