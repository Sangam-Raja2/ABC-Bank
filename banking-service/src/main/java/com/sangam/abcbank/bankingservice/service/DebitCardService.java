package com.sangam.abcbank.bankingservice.service;

import com.sangam.abcbank.bankingservice.dto.*;
import com.sangam.abcbank.bankingservice.exception.*;
import com.sangam.abcbank.bankingservice.model.*;
import com.sangam.abcbank.bankingservice.repository.AccountRepository;
import com.sangam.abcbank.bankingservice.repository.DebitCardRepository;
import com.sangam.abcbank.bankingservice.repository.TransactionRepository;
import java.security.SecureRandom;
import java.time.YearMonth;
import com.sangam.abcbank.common.dto.CommonUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int VALIDITY_YEARS = 4;

    @Transactional
    public DebitCardResponse issueCard(IssueDebitCardRequest request, Authentication authentication) {
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + request.getAccountNumber()));

        assertOwnerOrAdmin(account.getOwnerUsername(), principal);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException(
                    "Cannot issue a card for a non-active account: " + account.getAccountNumber());
        }

        boolean hasActiveCard = debitCardRepository
                .existsByAccountNumberAndStatus(account.getAccountNumber(), CardStatus.ACTIVE);

        if (hasActiveCard) {
            throw new InvalidAccountStateException(
                    "An active card already exists for account: " + account.getAccountNumber()
                             +". Only one active card is allowed per account." +
                            " if you want to issue a new card, please block or cancel the existing one first.");
        }

        String cardNumber = generateCardNumber();
        String cvv = generateCvv();

        DebitCard card = DebitCard.builder()
                .cardNumber(cardNumber)
                .accountNumber(account.getAccountNumber())
                .ownerUsername(account.getOwnerUsername())
                .cardHolderName(principal.getName())
                .expiryDate(YearMonth.now().plusYears(VALIDITY_YEARS))
                .cvvHash(passwordEncoder.encode(cvv))
                .status(CardStatus.ACTIVE)
                .dailyLimit(request.getDailyLimit())
                .build();

        DebitCard saved = debitCardRepository.save(card);

        // In production: securely deliver the CVV out-of-band (SMS/mailer), never log it.
        return toResponse(saved);
    }

    public List<DebitCardResponse> getMyCards(Authentication authentication) {
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        List<DebitCard> byOwnerUsername = debitCardRepository.findByOwnerUsername(principal.getUsername());
       return byOwnerUsername.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DebitCardResponse getCard(String cardNumber, Authentication authentication) {
        DebitCard card = findCardOrThrow(cardNumber);
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        assertOwnerOrAdmin(card.getOwnerUsername(), principal);
        return toResponse(card);
    }

    @Transactional
    public DebitCardResponse blockCard(String cardNumber, Authentication authentication) {
        DebitCard card = findCardOrThrow(cardNumber);
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        assertOwnerOrAdmin(card.getOwnerUsername(), principal);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(debitCardRepository.save(card));
    }

    @Transactional
    public DebitCardResponse activateCard(String cardNumber, Authentication authentication) {
        DebitCard card = findCardOrThrow(cardNumber);
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        assertOwnerOrAdmin(card.getOwnerUsername(), principal);

        if (isExpired(card)) {
            throw new InvalidCardOperationException("Cannot activate an expired card: " + cardNumber);
        }
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(debitCardRepository.save(card));
    }

    @Transactional
    public DebitCardResponse cancelCard(String cardNumber, Authentication authentication) {
        DebitCard card = findCardOrThrow(cardNumber);
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        assertOwnerOrAdmin(card.getOwnerUsername(), principal);
        card.setStatus(CardStatus.CANCELLED);
        return toResponse(debitCardRepository.save(card));
    }

    @Transactional
    public AccountResponse payWithCard(String cardNumber, CardPaymentRequest request, Authentication authentication) {
        DebitCard card = findCardOrThrow(cardNumber);
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        assertOwnerOrAdmin(card.getOwnerUsername(), principal);

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardInactiveException("Card " + cardNumber + " is not active (status: " + card.getStatus() + ")");
        }
        if (isExpired(card)) {
            card.setStatus(CardStatus.EXPIRED);
            debitCardRepository.save(card);
            throw new CardInactiveException("Card " + cardNumber + " has expired");
        }
        if (!passwordEncoder.matches(request.getCvv(), card.getCvvHash())) {
            throw new InvalidCardOperationException("Invalid CVV for card " + cardNumber);
        }

        BigDecimal spentToday = spentToday(card.getAccountNumber());
        if (spentToday.add(request.getAmount()).compareTo(card.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(
                    "Payment exceeds daily card limit of " + card.getDailyLimit()
                            + " (already spent " + spentToday + " today)");
        }

        Account account = accountRepository.findByAccountNumber(card.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + card.getAccountNumber()));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + account.getAccountNumber()
                            + " for card payment of " + request.getAmount());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountNumber(account.getAccountNumber())
                .type(TransactionType.CARD_PAYMENT)
                .amount(request.getAmount())
                .balanceAfter(savedAccount.getBalance())
                .performedBy(authentication.getName())
                .build();
        transactionRepository.save(transaction);

        return AccountResponse.builder()
                .id(savedAccount.getId())
                .accountNumber(savedAccount.getAccountNumber())
                .ownerUsername(savedAccount.getOwnerUsername())
                .accountHolderName(savedAccount.getAccountHolderName())
                .accountType(savedAccount.getAccountType())
                .balance(savedAccount.getBalance())
                .status(savedAccount.getStatus().name())
                .createdAt(savedAccount.getCreatedAt())
                .updatedAt(savedAccount.getUpdatedAt())
                .build();
    }

    private BigDecimal spentToday(String accountNumber) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return transactionRepository
                .findByAccountNumberAndTypeAndTimestampBetween(
                        accountNumber, TransactionType.CARD_PAYMENT, startOfDay, endOfDay)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CardIssueResponse issuePhysicalCard(Authentication authentication, IssueCardRequest request) {
        CommonUser principal = (CommonUser) authentication.getPrincipal();
        // Verify the account exists and belongs to this user
        Account account = accountRepository.findByAccountNumberAndOwnerUsername(
                        request.getAccountNumber(), principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String cardNumber = generateCardNumber();
        String rawCvv = generateCvv();
        String cvvHash = passwordEncoder.encode(rawCvv);

        DebitCard card = DebitCard.builder()
                .accountNumber(account.getAccountNumber())
                .ownerUsername(principal.getUsername())
                .cardHolderName(principal.getName())
                .cardNumber(cardNumber)
                .cvvHash(cvvHash)
                .expiryDate(YearMonth.now().plusYears(5))
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() :
                        new BigDecimal("50000.00"))
                .status(CardStatus.ACTIVE)
                .build();

        debitCardRepository.save(card);


        // Raw CVV only ever appears in this one response — never stored, never logged
        return CardIssueResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .cvv(rawCvv)
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .dailyLimit(card.getDailyLimit())
                .status(card.getStatus())
                .build();
    }

    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("4378"); // example BIN prefix
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCvv() {
        SecureRandom random = new SecureRandom();
        return String.format("%03d", random.nextInt(1000)); // 3-digit CVV
    }

    private boolean isExpired(DebitCard card) {
        return card.getExpiryDate().isBefore(YearMonth.now());
    }

    private DebitCard findCardOrThrow(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardNumber));
    }

    /** Same ownership rule as AccountService — consider extracting to a shared @Component. */
    private void assertOwnerOrAdmin(String resourceOwnerUsername, CommonUser principal) {

        boolean isAdmin = principal.getRoles().stream().anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (!isAdmin && !resourceOwnerUsername.equals(principal.getUsername())) {
            throw new AccessDeniedForAccountException("You do not have access to this card");
        }
    }

    private DebitCardResponse toResponse(DebitCard card) {
        return DebitCardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(mask(card.getCardNumber()))
                .accountNumber(card.getAccountNumber())
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus().name())
                .dailyLimit(card.getDailyLimit())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    private String mask(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}