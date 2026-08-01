package com.sangam.abcbank.bankingservice.service;

import com.sangam.abcbank.bankingservice.dto.KycMapper;
import com.sangam.abcbank.bankingservice.dto.KycRequestDto;
import com.sangam.abcbank.bankingservice.dto.KycResponseDto;
import com.sangam.abcbank.bankingservice.dto.KycStatusUpdateDto;
import com.sangam.abcbank.bankingservice.exception.ResourceNotFoundException;
import com.sangam.abcbank.bankingservice.model.KycDetails;
import com.sangam.abcbank.bankingservice.model.KycStatus;
import com.sangam.abcbank.bankingservice.repository.KycRepository;
import com.sangam.abcbank.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class  KycService {

    private final KycRepository kycRepository;
    private final KycMapper kycMapper;

    public KycResponseDto create(KycRequestDto request) {
        if (kycRepository.existsByCustomerId(request.getCustomerId())) {
            throw new DuplicateResourceException(
                    "KYC record already exists for customerId=" + request.getCustomerId());
        }
        if (kycRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new DuplicateResourceException("Document number already registered");
        }

        KycDetails saved = kycRepository.save(kycMapper.toEntity(request));
        return kycMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public KycResponseDto getById(String id) {
        return kycMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public KycResponseDto getByCustomerId(String customerId) {
        KycDetails entity = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No KYC record found for customerId=" + customerId));
        return kycMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<KycResponseDto> list(KycStatus status) {
        List<KycDetails> kycResponse = (status != null)
                ? kycRepository.findByStatus(status)
                : kycRepository.findAll();
        return kycMapper.toResponseList(kycResponse);
    }

    public KycResponseDto update(String id, KycRequestDto request) {
        KycDetails entity = findEntityById(id);

        // guard against changing customerId/documentNumber into one already used by another record
        kycRepository.findByCustomerId(request.getCustomerId()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException(
                        "customerId already in use by another KYC record");
            }
        });

        entity.setFullName(request.getFullName());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setDocumentType(request.getDocumentType());
        entity.setDocumentNumber(request.getDocumentNumber());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());
        // editing resets status back to PENDING so it goes through re-verification
        entity.setStatus(KycStatus.PENDING);
        entity.setRejectionReason(null);

        return kycMapper.toResponse(kycRepository.save(entity));
    }

    public KycResponseDto updateStatus(String id, KycStatusUpdateDto request) {
        KycDetails entity = findEntityById(id);

        if (request.getStatus() == KycStatus.REJECTED
                && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("rejectionReason is required when status is REJECTED");
        }

        entity.setStatus(request.getStatus());
        entity.setRejectionReason(
                request.getStatus() == KycStatus.REJECTED ? request.getRejectionReason() : null);

        return kycMapper.toResponse(kycRepository.save(entity));
    }

    public void delete(String id) {
        KycDetails entity = findEntityById(id);
        kycRepository.delete(entity);
    }

    private KycDetails findEntityById(String id) {
        return kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC record not found: " + id));
    }
}
