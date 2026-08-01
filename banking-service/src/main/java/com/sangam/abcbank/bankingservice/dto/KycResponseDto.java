package com.sangam.abcbank.bankingservice.dto;

import com.sangam.abcbank.bankingservice.model.DocumentType;
import com.sangam.abcbank.bankingservice.model.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponseDto {

    private String id;
    private String customerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private DocumentType documentType;
    private String documentNumber;
    private String address;
    private String phoneNumber;
    private String email;
    private KycStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
