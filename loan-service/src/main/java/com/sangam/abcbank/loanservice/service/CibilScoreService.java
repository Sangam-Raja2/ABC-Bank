package com.sangam.abcbank.loanservice.service;


import com.sangam.abcbank.common.dto.KycResponseDto;
import com.sangam.abcbank.common.dto.DocumentType;
import com.sangam.abcbank.common.dto.BureauScoreResult;
import com.sangam.abcbank.common.exception.CibilScoreFetchException;
import com.sangam.abcbank.common.util.MockCibilBureauClient;
import com.sangam.abcbank.loanservice.client.BankingServiceClient;
import com.sangam.abcbank.loanservice.dto.CibilScoreResponse;
import com.sangam.abcbank.loanservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CibilScoreService {

    @Autowired
    private final MockCibilBureauClient cibilBureauClient; // external bureau integration
    @Autowired
    private final BankingServiceClient bankingServiceClient;


    public CibilScoreResponse getScoreForUser(String username) {
        KycResponseDto kycResponseDto = Optional.of(bankingServiceClient.getCurrentCustomerDetails(username))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer details found for username: " + username));
        String documentNumber =null;
        if(kycResponseDto.getDocumentType().equals(DocumentType.PAN))
        {
            documentNumber = kycResponseDto.getDocumentNumber();
        } else {
            throw new ResourceNotFoundException(
                    "CIBIL score can only be fetched for PAN document type. Found: " + kycResponseDto.getDocumentType());
        }


        try {
            BureauScoreResult result = cibilBureauClient.fetchScore(documentNumber);
            return new CibilScoreResponse(username, result.score(), result.reportDate());
        } catch (Exception ex) {
            log.error("Failed to fetch CIBIL score for user {}: {}", username, ex.getMessage());
            throw new CibilScoreFetchException(
                    "Unable to retrieve CIBIL score for user: " + username, ex);
        }
    }
}