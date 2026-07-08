package com.sangam.abcbank.loanservice.service;

import com.sangam.abcbank.loanservice.dto.AuditLogResponse;
import com.sangam.abcbank.loanservice.dto.LoanApprovalRequest;
import com.sangam.abcbank.loanservice.dto.LoanResponse;
import com.sangam.abcbank.loanservice.dto.LoanRequest;
import com.sangam.abcbank.loanservice.model.Loan;
import com.sangam.abcbank.loanservice.model.LoanAudit;
import com.sangam.abcbank.loanservice.model.LoanStatus;
import com.sangam.abcbank.loanservice.repository.LoanAuditRepository;
import com.sangam.abcbank.loanservice.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanAuditRepository loanAuditRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    public LoanResponse applyLoan(LoanRequest request, Authentication authentication) {


        Loan loan = Loan.builder()
                .loanAccountNumber(generateAccountNumber())
                .ownerUsername(authentication.getName())
                .loanAmount(request.getLoanAmount())
                .tenureInMonths(request.getTenureInMonths())
                .annualInterestRate(request.getAnnualInterestRate())
                .loanPurpose(request.getLoanPurpose())
                .status(LoanStatus.PENDING)
                .appliedDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        Loan savedLoan = loanRepository.save(loan);
        saveAuditLog(savedLoan, "APPLIED", authentication, "Loan application submitted.");

        return LoanResponse.builder()
                .loanAccountNumber(savedLoan.getLoanAccountNumber())
                .ownerUsername(savedLoan.getOwnerUsername())
                .loanAmount(savedLoan.getLoanAmount())
                .tenureInMonths(savedLoan.getTenureInMonths())
                .annualInterestRate(savedLoan.getAnnualInterestRate())
                .loanPurpose(savedLoan.getLoanPurpose())
                .status(savedLoan.getStatus().name())
                .appliedDate(savedLoan.getAppliedDate())
                .build();
    }

    public LoanResponse reviewLoan(String loanAccountNumber, LoanApprovalRequest request,
                                   Authentication authentication) {
        Loan loan = validateLoan(loanAccountNumber);

        loan.setStatus(LoanStatus.UNDER_REVIEW);
        loan.setReviewedByName(authentication.getName());
        loan.setReviewersRole(authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN"));
        loan.setReviewedDate(LocalDateTime.now());
        loan.setReviewalRemarks(request.getRemarks());
        loan.setUpdatedDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);
        saveAuditLog(savedLoan, "UNDER_REVIEW", authentication, "Loan application rejected.");
        return mapToResponseWithDetails(savedLoan);

    }

    public LoanResponse approveLoan(String loanAccountNumber,
                                    LoanApprovalRequest request, Authentication authentication) {
        Loan loan = validateLoan(loanAccountNumber);

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedByName(authentication.getName());
        loan.setApproversRole(authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN"));
        loan.setApprovedDate(LocalDateTime.now());
        loan.setApprovalRemarks(request.getRemarks());
        loan.setUpdatedDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);
        saveAuditLog(savedLoan, "APPROVED", authentication, "Loan application approved.");

        return mapToResponseWithDetails(savedLoan);
    }

    public LoanResponse rejectLoan(String loanAccountNumber, LoanApprovalRequest request,
                                   Authentication authentication) {
        Loan loan = validateLoan(loanAccountNumber);

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectedByName(authentication.getName());
        loan.setRejectedDate(LocalDateTime.now());
        loan.setRejectorsRole(authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN"));
        loan.setRejectedRemarks(request.getRemarks());
        loan.setUpdatedDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);
        saveAuditLog(savedLoan, "REJECTED", authentication, "Loan application rejected.");

        return mapToResponseWithDetails(savedLoan);
    }

    public LoanResponse disburseLoan(String loanAccountNumber, Authentication authentication) {

        Loan loan = loanRepository.findById(loanAccountNumber)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found with loan AccountNumber : " + loanAccountNumber));

        // Loan must be approved first
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Only approved loan can be disbursed.");
        }

        // Prevent duplicate disbursement
        if (loan.getDisbursedDate() != null) {
            throw new RuntimeException("Loan has already been disbursed.");
        }

        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursedByName(authentication.getName());
        loan.setDisbursedDate(LocalDateTime.now());
        loan.setDisbursedByRole(authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN"));
        Loan savedLoan = loanRepository.save(loan);
        saveAuditLog(savedLoan, "DISBURSED", authentication, "Loan disbursed.");

        return mapToResponseWithDetails(savedLoan);
    }

    public List<LoanResponse> getMyApplications(String ownerUsername) {

        List<Loan> loans = loanRepository.findByOwnerUsername(ownerUsername);

        return loans.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public Boolean deleteLoan(String loanAccountNumber,Authentication authentication) {
        // Check if the loan exists
        Optional<Loan> loan = loanRepository.findById(loanAccountNumber);

        if (loan.isPresent()) {
            Loan existingLoan = Loan.builder()
                    .loanAccountNumber(loan.get().getLoanAccountNumber())
                    .build();
            loanRepository.deleteById(loanAccountNumber);
            saveAuditLog(existingLoan, "DELETED", authentication, "Loan application deleted.");
            return true; // Deletion was successful
        }
        return false; // Loan with the given ID does not exist
    }

    private LoanResponse mapToResponse(Loan loan) {

        return LoanResponse.builder()
                .ownerUsername(loan.getOwnerUsername())
                .loanAmount(loan.getLoanAmount())
                .loanPurpose(loan.getLoanPurpose())
                .status(loan.getStatus().name())
                .appliedDate(loan.getAppliedDate())
                .approvedDate(loan.getApprovedDate())
                .disbursedDate(loan.getDisbursedDate())
                .build();
    }


    public List<AuditLogResponse> getAuditLogs(String loanAccountNumber) {

        return loanAuditRepository.findByLoanAccountNumber(loanAccountNumber)
                .stream()
                .map(audit -> AuditLogResponse.builder()
                        .id(audit.getId())
                        .loanAccountNumber(audit.getLoanAccountNumber())
                        .action(audit.getAction())
                        .performedBy(audit.getPerformedBy())
                        .role(audit.getRole())
                        .actionTime(audit.getActionTime())
                        .remarks(audit.getRemarks())
                        .build())
                .toList();
    }


    private AuditLogResponse saveAuditLog(Loan loan,
                              String action,
                              Authentication authentication,
                              String remarks) {

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN");

        LoanAudit audit = LoanAudit.builder()
                .loanAccountNumber(loan.getLoanAccountNumber())
                .action(action)
                .performedBy(authentication.getName())
                .role(role)
                .actionTime(LocalDateTime.now())
                .remarks(remarks)
                .build();
        loanAuditRepository.save(audit);
        return AuditLogResponse.builder()
                .id(audit.getId())
                .loanAccountNumber(audit.getLoanAccountNumber())
                .action(audit.getAction())
                .performedBy(audit.getPerformedBy())
                .role(audit.getRole())
                .actionTime(audit.getActionTime())
                .remarks(audit.getRemarks())
                .build();
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "ABC" + String.format("%010d", Math.abs(RANDOM.nextLong() % 10_000_000_000L));
        } while (loanRepository
                .existsById(candidate));
        return candidate;
    }

    private LoanResponse mapToResponseWithDetails(Loan loan) {
        return LoanResponse.builder()
                .loanAccountNumber(loan.getLoanAccountNumber())
                .ownerUsername(loan.getOwnerUsername())
                .loanAmount(loan.getLoanAmount())
                .tenureInMonths(loan.getTenureInMonths())
                .annualInterestRate(loan.getAnnualInterestRate())
                .loanPurpose(loan.getLoanPurpose())
                .status(loan.getStatus().name())
                .appliedDate(loan.getAppliedDate())
                .reviewedByName(loan.getReviewedByName())
                .reviewersRole(loan.getReviewersRole())
                .reviewedDate(loan.getReviewedDate())
                .reviewalRemarks(loan.getReviewalRemarks())
                .approvedByName(loan.getApprovedByName())
                .approversRole(loan.getApproversRole())
                .approvedDate(loan.getApprovedDate())
                .approvalRemarks(loan.getApprovalRemarks())
                .rejectedByName(loan.getRejectedByName())
                .rejectorsRole(loan.getRejectorsRole())
                .rejectedDate(loan.getRejectedDate())
                .rejectedRemarks(loan.getRejectedRemarks())
                .disbursedByName(loan.getDisbursedByName())
                .disbursedByRole(loan.getDisbursedByRole())
                .disbursedDate(loan.getDisbursedDate())
                .build();
    }

    private Loan validateLoan(String loanAccountNumber) {
        Loan loan = loanRepository.findById(loanAccountNumber)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new RuntimeException(
                    "Not Processed! Only PENDING or UNDER REVIEW loan can be reviewed, processed or rejected.");
        }
        return loan;
    }
}