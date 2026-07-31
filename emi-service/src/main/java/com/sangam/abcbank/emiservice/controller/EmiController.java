package com.sangam.abcbank.emiservice.controller;


import com.sangam.abcbank.emiservice.dto.EmiCalculationRequest;
import com.sangam.abcbank.emiservice.dto.EmiCalculationResponse;
import com.sangam.abcbank.emiservice.dto.LoanDto;
import com.sangam.abcbank.emiservice.entity.EmiPayment;
import com.sangam.abcbank.emiservice.service.EmiCalculatorService;
import com.sangam.abcbank.emiservice.service.EmiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
public class EmiController {

    private final EmiService emiService;
    private final EmiCalculatorService emiCalculatorService;

    /**
     * Standalone EMI calculator - no loan lookup required.
     * Any authenticated user can use this (e.g. "what would my EMI be if I borrowed X").
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER','LOAN_OFFICER','MANAGER')")
    public ResponseEntity<EmiCalculationResponse> calculate(@Valid @RequestBody EmiCalculationRequest request) {
        return ResponseEntity.ok(emiCalculatorService.calculate(request));
    }

    /**
     * Fetch a loan by id via loan-service.
     * Access rule: allowed only if the caller's username matches the loan owner's
     * username, OR the caller has ROLE_ADMIN. Enforced by EmiSecurityService.canAccessLoan().
     */
    @GetMapping("/loans/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER','LOAN_OFFICER','MANAGER')")
    public ResponseEntity<LoanDto> getLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getLoan(loanId));
    }

    /**
     * Calculate the EMI for a specific existing loan (pulls principal/rate/tenure from loan-service).
     * Same ownership/admin restriction as above.
     */
    @GetMapping("/loans/{loanId}/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER','LOAN_OFFICER','MANAGER')")
    public ResponseEntity<EmiCalculationResponse> calculateForLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.calculateEmiForLoan(loanId));
    }

    /**
     * Manually trigger payment of the next due EMI right now (checks banking-service balance
     * first; only debits if sufficient - same logic the nightly scheduler uses).
     * Same ownership/admin restriction as above.
     */
    @PostMapping("/loans/{loanId}/pay-next")
    @PreAuthorize("hasAnyRole( 'CUSTOMER')")
    public ResponseEntity<EmiPayment> payNextEmi(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.payNextEmi(loanId));
    }
}
