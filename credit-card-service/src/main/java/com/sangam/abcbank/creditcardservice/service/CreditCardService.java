package com.sangam.abcbank.creditcardservice.service;

import com.sangam.abcbank.creditcardservice.dto.CreditCardIssueResponse;
import com.sangam.abcbank.creditcardservice.dto.CreditCardResponse;
import com.sangam.abcbank.creditcardservice.dto.IssueCreditCardRequest;
import com.sangam.abcbank.creditcardservice.exception.DuplicateCardException;
import com.sangam.abcbank.creditcardservice.exception.ResourceNotFoundException;
import com.sangam.abcbank.creditcardservice.model.CardStatus;
import com.sangam.abcbank.creditcardservice.model.CreditCard;
import com.sangam.abcbank.creditcardservice.repository.CreditCardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.List;

@Service
public class CreditCardService {

    private static final int VALIDITY_YEARS = 5;
    // ASSUMPTION: replace with your real BIN prefix for credit cards
    private static final String BIN_PREFIX = "5241";

    private final CreditCardRepository creditCardRepository;
    private final PasswordEncoder passwordEncoder;

    public CreditCardService(CreditCardRepository creditCardRepository, PasswordEncoder passwordEncoder) {
        this.creditCardRepository = creditCardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreditCardIssueResponse issueCard(IssueCreditCardRequest request, Authentication authentication) {
        String username = authentication.getName();

        boolean hasActiveCard = creditCardRepository
                .existsByOwnerUsernameAndStatus(username, CardStatus.ACTIVE);

        if (hasActiveCard) {
            throw new DuplicateCardException(
                    "An active credit card already exists for user: " + username);
        }

        String cardNumber = generateCardNumber();
        String rawCvv = generateCvv();

        CreditCard card = CreditCard.builder()
                .cardNumber(cardNumber)
                .ownerUsername(username)
                .cardHolderName(username) // ASSUMPTION: swap for a display-name claim if user-service provides one
                .expiryDate(YearMonth.now().plusYears(VALIDITY_YEARS))
                .cvvHash(passwordEncoder.encode(rawCvv))
                .status(CardStatus.ACTIVE)
                .creditLimit(request.creditLimit())
                .availableCredit(request.creditLimit())
                .billingCycleDay(request.billingCycleDay())
                .build();

        CreditCard saved = creditCardRepository.save(card);

        // rawCvv only ever appears in this one response — never stored, never logged
        return CreditCardIssueResponse.builder()
                .id(saved.getId())
                .cardNumber(saved.getCardNumber())
                .cvv(rawCvv)
                .cardHolderName(saved.getCardHolderName())
                .expiryDate(saved.getExpiryDate())
                .creditLimit(saved.getCreditLimit())
                .billingCycleDay(saved.getBillingCycleDay())
                .status(saved.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CreditCardResponse> getMyCards(Authentication authentication) {
        String username = authentication.getName();
        return creditCardRepository.findAllByOwnerUsername(username).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditCardResponse getCard(Long cardId, Authentication authentication) {
        String username = authentication.getName();
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        if (!card.getOwnerUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this card");
        }

        return toResponse(card);
    }

    private CreditCardResponse toResponse(CreditCard card) {
        return CreditCardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .creditLimit(card.getCreditLimit())
                .availableCredit(card.getAvailableCredit())
                .billingCycleDay(card.getBillingCycleDay())
                .status(card.getStatus())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(BIN_PREFIX);
        for (int i = 0; i < 16 - BIN_PREFIX.length(); i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCvv() {
        SecureRandom random = new SecureRandom();
        return String.format("%03d", random.nextInt(1000));
    }
}
