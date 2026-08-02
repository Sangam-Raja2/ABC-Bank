package com.sangam.abcbank.emiservice.dto;

import com.sangam.abcbank.emiservice.entity.EmiTransition.SourceType;
import com.sangam.abcbank.emiservice.entity.EmiTransition.TransitionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EmiTransitionResponse(
        Long id,
        String username,
        SourceType sourceType,
        String sourceReferenceId,
        BigDecimal principal,
        BigDecimal annualInterestRate,
        int tenureMonths,
        BigDecimal monthlyEmi,
        TransitionStatus status,
        String remarks,
        LocalDateTime createdAt
) {}
