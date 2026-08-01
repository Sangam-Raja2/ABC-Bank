package com.sangam.abcbank.bankingservice.controller;


import com.sangam.abcbank.bankingservice.dto.KycRequestDto;
import com.sangam.abcbank.bankingservice.dto.KycResponseDto;
import com.sangam.abcbank.bankingservice.dto.KycStatusUpdateDto;
import com.sangam.abcbank.bankingservice.model.KycStatus;
import com.sangam.abcbank.bankingservice.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    // Create a new KYC record
    @PostMapping
    public ResponseEntity<KycResponseDto> create(@Valid @RequestBody KycRequestDto request) {
        KycResponseDto created = kycService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/kyc/" + created.getId())).body(created);
    }

    // Fetch a KYC record by its internal id
    @GetMapping("/{id}")
    public ResponseEntity<KycResponseDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(kycService.getById(id));
    }

    // Fetch a KYC record by customer id
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<KycResponseDto> getByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(kycService.getByCustomerId(customerId));
    }

    // List/paginate KYC records, optionally filtered by status
    @GetMapping
    public ResponseEntity<List<KycResponseDto>> list(
            @RequestParam(required = false) KycStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(kycService.list(status));
    }

    // Full update of a KYC record (resets status to PENDING for re-verification)
    @PutMapping("/{id}")
    public ResponseEntity<KycResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody KycRequestDto request) {
        return ResponseEntity.ok(kycService.update(id, request));
    }

    // Verify/reject a KYC record (typically restricted to compliance/admin role)
    @PatchMapping("/{id}/status")
    public ResponseEntity<KycResponseDto> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody KycStatusUpdateDto request) {
        return ResponseEntity.ok(kycService.updateStatus(id, request));
    }

    // Delete a KYC record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        kycService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
