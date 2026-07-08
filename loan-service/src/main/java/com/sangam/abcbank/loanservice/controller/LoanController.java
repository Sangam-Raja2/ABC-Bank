package com.sangam.abcbank.loanservice.controller;

import com.sangam.abcbank.loanservice.dto.LoanApprovalRequest;
import com.sangam.abcbank.loanservice.dto.LoanResponse;
import com.sangam.abcbank.loanservice.dto.LoanRequest;
import com.sangam.abcbank.loanservice.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    /** apply for loan. Owned by the currently authenticated user. */
    private final LoanService loanService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse applyLoan(
            @Valid @RequestBody LoanRequest request,Authentication authentication) {

        return loanService.applyLoan(request,authentication);
    }

    @PreAuthorize("hasRole('LOAN_OFFICER')")
    @PutMapping("/{id}/approve")
    public LoanResponse approveLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanApprovalRequest request,Authentication authentication) {

        return loanService.approveLoan(id, request,authentication);
    }


    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/disburse")
    public LoanResponse disburseLoan(@PathVariable Long id,Authentication authentication) {

        return loanService.disburseLoan(id,authentication);
    }

    /** List Loan accounts owned by the currently authenticated user. */
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanResponse>> getMyApplications(Authentication authentication) {

        String userName = authentication.getName();

        List<LoanResponse> response = loanService.getMyApplications(userName);

        return ResponseEntity.ok(response);
    }
}
