// exception/DailyLimitExceededException.java
package com.sangam.abcbank.bankingservice.exception;

public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) { super(message); }
}