package com.sangam.abcbank.loanservice.controller;


import com.sangam.abcbank.loanservice.dto.AuditLogResponse;
import com.sangam.abcbank.loanservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LoanService loanService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit-logs/{loanAccountNumber}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(@PathVariable String loanAccountNumber) {

        return ResponseEntity.ok(loanService.getAuditLogs(loanAccountNumber));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanAccountNumber}")
    public ResponseEntity<Void> deleteLoan(@PathVariable String loanAccountNumber,Authentication authentication) {
        boolean deleted = loanService.deleteLoan(loanAccountNumber, authentication);
        if (!deleted) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
