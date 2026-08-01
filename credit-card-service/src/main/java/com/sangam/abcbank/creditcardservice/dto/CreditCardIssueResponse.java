package com.sangam.abcbank.creditcardservice.dto;

import com.sangam.abcbank.creditcardservice.model.CardStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Shown exactly once, at issuance. cvv is never persisted or logged after this point —
 * do not add a toString()/logging call anywhere that could print this object.
 */
@Builder
public record CreditCardIssueResponse(
        Long id,
        String cardNumber,
        String cvv,
        String cardHolderName,
        YearMonth expiryDate,
        BigDecimal creditLimit,
        Integer billingCycleDay,
        CardStatus status
) {
    @Override
    public String toString() {
        return "CreditCardIssueResponse{id=" + id + ", cardNumber=**** masked, cvv=***, status=" + status + "}";
    }
}
