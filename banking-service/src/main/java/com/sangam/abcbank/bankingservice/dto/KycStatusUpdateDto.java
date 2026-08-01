package com.sangam.abcbank.bankingservice.dto;

import com.sangam.abcbank.bankingservice.model.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycStatusUpdateDto {

    @NotNull(message = "status is required")
    private KycStatus status;

    // required when status = REJECTED
    private String rejectionReason;
}
