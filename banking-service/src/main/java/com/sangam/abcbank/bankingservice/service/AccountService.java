package com.sangam.abcbank.bankingservice.service;

import com.sangam.abcbank.bankingservice.dto.*;
import com.sangam.abcbank.bankingservice.exception.*;
import com.sangam.abcbank.bankingservice.model.Account;
import com.sangam.abcbank.bankingservice.model.AccountStatus;
import com.sangam.abcbank.bankingservice.model.Transaction;
import com.sangam.abcbank.bankingservice.model.TransactionType;
import com.sangam.abcbank.bankingservice.repository.AccountRepository;
import com.sangam.abcbank.bankingservice.repository.TransactionRepository;
import com.sangam.abcbank.dto.CommonUser;
import com.sangam.abcbank.util.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, Authentication authentication) {
        String accountNumber = generateAccountNumber();
        CommonUser commonUser = Utility.getFromPrincipal(authentication);

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .ownerUsername(commonUser.getUsername())
                .accountHolderName(commonUser.getName())
                .accountType(request.getAccountType().toUpperCase())
                .balance(request.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);

        if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved.getAccountNumber(), TransactionType.DEPOSIT,
                    request.getInitialDeposit(), saved.getBalance(), authentication.getName());
        }

        return toResponse(saved);
    }

    public AccountResponse getAccount(String accountNumber, Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);
        return toResponse(account);
    }

    public List<AccountResponse> getMyAccounts(Authentication authentication) {
        CommonUser principal =(CommonUser) authentication.getPrincipal();
        return accountRepository.findByOwnerUsername(principal.getUsername())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateAccount(String accountNumber, UpdateAccountRequest request,
                                          Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);

        if (request.getAccountHolderName() != null && !request.getAccountHolderName().isBlank()) {
            account.setAccountHolderName(request.getAccountHolderName());
        }
        if (request.getAccountType() != null && !request.getAccountType().isBlank()) {
            account.setAccountType(request.getAccountType().toUpperCase());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                account.setStatus(AccountStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new InvalidAccountStateException("Invalid status. Must be one of ACTIVE, INACTIVE, CLOSED");
            }
        }

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse deposit(String accountNumber, AmountRequest request, Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);
        assertActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account saved = accountRepository.save(account);

        recordTransaction(accountNumber, TransactionType.DEPOSIT, request.getAmount(),
                saved.getBalance(), authentication.getName());

        return toResponse(saved);
    }

    @Transactional
    public AccountResponse withdraw(String accountNumber, AmountRequest request, Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);
        assertActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + accountNumber + " for withdrawal of " + request.getAmount());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account saved = accountRepository.save(account);

        recordTransaction(accountNumber, TransactionType.WITHDRAWAL, request.getAmount(),
                saved.getBalance(), authentication.getName());

        return toResponse(saved);
    }

    public BalanceResponse getBalance(String accountNumber, Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);
        return BalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber, Authentication authentication) {
        Account account = findAccountOrThrow(accountNumber);
        assertOwnerOrAdmin(account, authentication);

        return transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber).stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .accountNumber(t.getAccountNumber())
                        .type(t.getType().name())
                        .amount(t.getAmount())
                        .balanceAfter(t.getBalanceAfter())
                        .performedBy(t.getPerformedBy())
                        .timestamp(t.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }


    private void recordTransaction(String accountNumber, TransactionType type, BigDecimal amount,
                                    BigDecimal balanceAfter, String performedBy) {
        Transaction transaction = Transaction.builder()
                .accountNumber(accountNumber)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .performedBy(performedBy)
                .build();
        transactionRepository.save(transaction);
    }

    private Account findAccountOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private void assertActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException(
                    "Account " + account.getAccountNumber() + " is not ACTIVE (current status: " + account.getStatus() + ")");
        }
    }

    /**
     * Enforces that only the account owner or a user with ROLE_ADMIN may access/modify the account.
     */
    private void assertOwnerOrAdmin(Account account, Authentication authentication) {
        CommonUser principal =(CommonUser) authentication.getPrincipal();
        boolean isAdmin = principal.getRoles().stream()
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getOwnerUsername().equals(authentication.getName())) {
            throw new AccessDeniedForAccountException("You do not have access to account " + account.getAccountNumber());
        }
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "ABC" + String.format("%010d", Math.abs(RANDOM.nextLong() % 10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerUsername(account.getOwnerUsername())
                .accountHolderName(account.getAccountHolderName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
