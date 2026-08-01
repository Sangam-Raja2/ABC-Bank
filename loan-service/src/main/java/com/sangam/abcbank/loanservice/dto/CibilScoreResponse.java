package com.sangam.abcbank.loanservice.dto;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record CibilScoreResponse(
        String username, int score,
        LocalDate reportDate) {}