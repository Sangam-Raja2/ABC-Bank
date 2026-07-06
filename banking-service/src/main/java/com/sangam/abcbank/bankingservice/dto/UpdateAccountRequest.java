package com.sangam.abcbank.bankingservice.dto;

import lombok.Data;

@Data
public class UpdateAccountRequest {
    private String accountHolderName;
    private String accountType;
    private String status; // ACTIVE, INACTIVE, CLOSED
}
