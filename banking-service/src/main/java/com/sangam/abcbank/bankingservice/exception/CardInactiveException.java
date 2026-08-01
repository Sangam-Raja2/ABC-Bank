// exception/CardInactiveException.java
package com.sangam.abcbank.bankingservice.exception;

public class CardInactiveException extends RuntimeException {
    public CardInactiveException(String message) { super(message); }
}