package com.sangam.abcbank.loanservice.service;

import com.sangam.abcbank.loanservice.dto.AuditLogResponse;
import com.sangam.abcbank.loanservice.dto.LoanApprovalRequest;
import com.sangam.abcbank.loanservice.dto.LoanResponse;
import com.sangam.abcbank.loanservice.dto.LoanRequest;
import com.sangam.abcbank.loanservice.model.Loan;
import com.sangam.abcbank.loanservice.model.LoanStatus;
import com.sangam.abcbank.loanservice.repository.LoanAuditRepository;
import com.sangam.abcbank.loanservice.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanAuditRepository loanAuditRepository;

    public LoanResponse applyLoan(LoanRequest request, Authentication authentication) {


        Loan loan = Loan.builder()
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

        return LoanResponse.builder()
                .loanId(savedLoan.getId())
                .ownerUsername(savedLoan.getOwnerUsername())
                .loanAmount(savedLoan.getLoanAmount())
                .tenureInMonths(savedLoan.getTenureInMonths())
                .annualInterestRate(savedLoan.getAnnualInterestRate())
                .loanPurpose(savedLoan.getLoanPurpose())
                .status(savedLoan.getStatus().name())
                .appliedDate(savedLoan.getAppliedDate())
                .build();
    }

    public LoanResponse approveLoan(Long loanId,
                                    LoanApprovalRequest request, Authentication authentication) {



        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING loan can be approved.");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedByName(authentication.getName());
        loan.setApprovedDate(LocalDateTime.now());
        loan.setApprovalRemarks(request.getApprovalRemarks());
        loan.setUpdatedDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponse.builder()
                .loanId(savedLoan.getId())
                .ownerUsername(savedLoan.getOwnerUsername())
                .loanAmount(savedLoan.getLoanAmount())
                .tenureInMonths(savedLoan.getTenureInMonths())
                .annualInterestRate(savedLoan.getAnnualInterestRate())
                .loanPurpose(savedLoan.getLoanPurpose())
                .status(savedLoan.getStatus().name())
                .approvedByName(authentication.getName())
                .approvedDate(savedLoan.getApprovedDate())
                .approvalRemarks(savedLoan.getApprovalRemarks())
                .build();
    }

    public LoanResponse disburseLoan(Long loanId, Authentication authentication) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found with id : " + loanId));

        // Loan must be approved first
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Only approved loan can be disbursed.");
        }

        // Prevent duplicate disbursement
        if (loan.getDisbursedDate() != null) {
            throw new RuntimeException("Loan has already been disbursed.");
        }

        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursedDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponse.builder()
                .loanId(savedLoan.getId())
                .ownerUsername(savedLoan.getOwnerUsername())
                .loanAmount(savedLoan.getLoanAmount())
                .tenureInMonths(savedLoan.getTenureInMonths())
                .annualInterestRate(savedLoan.getAnnualInterestRate())
                .loanPurpose(savedLoan.getLoanPurpose())
                .status(savedLoan.getStatus().name())
                .approvedByName(loan.getApprovedByName())
                .approvedDate(savedLoan.getApprovedDate())
                .approvalRemarks(savedLoan.getApprovalRemarks())
                .disbursedByName(authentication.getName())
                .disbursedDate(savedLoan.getDisbursedDate())
                .build();
    }

    public List<LoanResponse> getMyApplications(String ownerUsername) {

        List<Loan> loans = loanRepository.findByOwnerUsername(ownerUsername);

        return loans.stream()
                .map(this::mapToResponse)
                .toList();
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


    public List<AuditLogResponse> getAuditLogs() {

        return loanAuditRepository.findAll()
                .stream()
                .map(audit -> AuditLogResponse.builder()
                        .id(audit.getId())
                        .loanId(audit.getLoanId())
                        .action(audit.getAction())
                        .performedBy(audit.getPerformedBy())
                        .role(audit.getRole())
                        .actionTime(audit.getActionTime())
                        .remarks(audit.getRemarks())
                        .build())
                .toList();
    }
}