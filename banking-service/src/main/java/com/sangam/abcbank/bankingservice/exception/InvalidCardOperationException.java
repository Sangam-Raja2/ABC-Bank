// exception/InvalidCardOperationException.java
package com.sangam.abcbank.bankingservice.exception;

public class InvalidCardOperationException extends RuntimeException {
    public InvalidCardOperationException(String message) { super(message); }
}