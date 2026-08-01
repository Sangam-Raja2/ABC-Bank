// exception/CardNotFoundException.java
package com.sangam.abcbank.bankingservice.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) { super(message); }
}