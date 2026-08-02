package com.sangam.abcbank.emiservice.dto;

import java.time.LocalDateTime;

public record CibilScoreResponse(
        String username,
        String panNumber,
        int score,           // CIBIL score range: 300-900
        String riskGrade,    // POOR / FAIR / GOOD / VERY_GOOD / EXCELLENT
        String provider,     // "mock" until real bureau integration is wired in
        LocalDateTime checkedAt
) {}
