package com.sangam.abcbank.emiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CibilScoreRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "panNumber must be a valid PAN, e.g. ABCDE1234F")
        String panNumber
) {}
