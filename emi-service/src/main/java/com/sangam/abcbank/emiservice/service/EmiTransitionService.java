package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.common.dto.CommonUser;
import com.sangam.abcbank.common.util.Utility;
import com.sangam.abcbank.emiservice.dto.EmiCalculatorRequest;
import com.sangam.abcbank.emiservice.dto.EmiCalculatorResponse;
import com.sangam.abcbank.emiservice.dto.EmiTransitionRequest;
import com.sangam.abcbank.emiservice.dto.EmiTransitionResponse;
import com.sangam.abcbank.emiservice.entity.EmiTransition;
import com.sangam.abcbank.emiservice.entity.EmiTransition.SourceType;
import com.sangam.abcbank.emiservice.entity.EmiTransition.TransitionStatus;
import com.sangam.abcbank.emiservice.exception.TransitionNotAllowedException;
import com.sangam.abcbank.emiservice.repository.EmiTransitionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmiTransitionService {

    private final EmiTransitionRepository repository;
    private final EmiCalculatorService calculatorService;
    private final DownstreamBankingClient downstreamClient;

    public EmiTransitionService(EmiTransitionRepository repository,
                                EmiCalculatorService calculatorService,
                                DownstreamBankingClient downstreamClient) {
        this.repository = repository;
        this.calculatorService = calculatorService;
        this.downstreamClient = downstreamClient;
    }

    @Transactional
    public EmiTransitionResponse initiateTransition(Authentication principal, String bearerToken, EmiTransitionRequest request) {

        CommonUser user = Utility.getFromPrincipal(principal);
        String username = user.getUsername();

        boolean verified = request.sourceType() == SourceType.CREDIT_CARD
                ? downstreamClient.verifyCreditCardTransaction(request.sourceReferenceId(), bearerToken)
                : downstreamClient.verifyLoanAccount(request.sourceReferenceId(), bearerToken);

        if (!verified) {
            throw new TransitionNotAllowedException(
                    "Could not verify " + request.sourceType() + " reference " + request.sourceReferenceId()
                            + " against the source service. Transition rejected.");
        }

        EmiCalculatorResponse calc = calculatorService.calculate(
                new EmiCalculatorRequest(request.amount(), request.annualInterestRate(), request.tenureMonths()));

        // Actually pull the converted amount out of the source ledger. If this fails, the
        // whole transition is rejected - the exception propagates before repository.save,
        // so @Transactional rolls back and no EmiTransition row is persisted.
        try {
            if (request.sourceType() == SourceType.CREDIT_CARD) {
                downstreamClient.adjustCreditCardBill(request.sourceReferenceId(), request.amount(), bearerToken);
            } else {
                downstreamClient.adjustLoanAmount(request.sourceReferenceId(), request.amount(), bearerToken);
            }
        } catch (DownstreamBankingClient.DownstreamAdjustmentException e) {
            throw new TransitionNotAllowedException(
                    "Verified " + request.sourceType() + " reference " + request.sourceReferenceId()
                            + " but failed to adjust the outstanding balance in the source service: "
                            + e.getMessage() + ". Transition rejected, no EMI schedule created.");
        }

        EmiTransition transition = EmiTransition.builder()
                .username(username)
                .sourceType(request.sourceType())
                .sourceReferenceId(request.sourceReferenceId())
                .principal(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .tenureMonths(request.tenureMonths())
                .monthlyEmi(calc.monthlyEmi())
                .status(TransitionStatus.CONFIRMED)
                .remarks("Converted to EMI over " + request.tenureMonths() + " months; "
                        + request.sourceType() + " outstanding reduced by " + request.amount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmiTransition saved = repository.save(transition);
        return toResponse(saved);
    }

    public List<EmiTransitionResponse> getHistory(String username) {
        return repository.findByUsername(username).stream().map(this::toResponse).toList();
    }

    private EmiTransitionResponse toResponse(EmiTransition t) {
        return new EmiTransitionResponse(
                t.getId(), t.getUsername(), t.getSourceType(), t.getSourceReferenceId(),
                t.getPrincipal(), t.getAnnualInterestRate(), t.getTenureMonths(),
                t.getMonthlyEmi(), t.getStatus(), t.getRemarks(), t.getCreatedAt());
    }
}
