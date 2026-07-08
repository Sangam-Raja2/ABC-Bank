package com.sangam.abcbank.loanservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoanApprovalRequest {

    @NotBlank
    private String remarks;
}
