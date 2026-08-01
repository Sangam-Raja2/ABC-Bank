package com.sangam.abcbank.creditcardservice.dto;

import com.sangam.abcbank.creditcardservice.model.CardStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;

@Builder
public record CreditCardResponse(
        Long id,
        String maskedCardNumber,
        String cardHolderName,
        YearMonth expiryDate,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        Integer billingCycleDay,
        CardStatus status
) {}
