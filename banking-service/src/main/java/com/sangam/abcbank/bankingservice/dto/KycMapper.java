package com.sangam.abcbank.bankingservice.dto;

import com.sangam.abcbank.bankingservice.model.KycDetails;
import com.sangam.abcbank.bankingservice.model.KycStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KycMapper {

    public KycDetails toEntity(KycRequestDto dto) {
        return KycDetails.builder()
                .customerId(dto.getCustomerId())
                .fullName(dto.getFullName())
                .dateOfBirth(dto.getDateOfBirth())
                .documentType(dto.getDocumentType())
                .documentNumber(dto.getDocumentNumber())
                .address(dto.getAddress())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .status(KycStatus.PENDING)
                .build();
    }

    public KycResponseDto toResponse(KycDetails entity) {
        return KycResponseDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .fullName(entity.getFullName())
                .dateOfBirth(entity.getDateOfBirth())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<KycResponseDto> toResponseList(List<KycDetails> entities) {
        return entities.stream().map(entity-> KycResponseDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .fullName(entity.getFullName())
                .dateOfBirth(entity.getDateOfBirth())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build()).collect(Collectors.toList());
    }

    private String mask(String documentNumber) {
        if (documentNumber == null || documentNumber.length() <= 4) {
            return "****";
        }
        int visible = 4;
        String tail = documentNumber.substring(documentNumber.length() - visible);
        return "*".repeat(documentNumber.length() - visible) + tail;
    }
}
