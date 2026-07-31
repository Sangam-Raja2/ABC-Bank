package com.sangam.abcbank.emiservice.client;

import com.sangam.abcbank.emiservice.config.FeignAuthInterceptor;
import com.sangam.abcbank.emiservice.dto.BankAccountDto;
import com.sangam.abcbank.emiservice.dto.DebitRequest;
import com.sangam.abcbank.emiservice.dto.DebitResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ASSUMPTIONS about banking-service's exposed API (adjust paths once confirmed from the repo):
 *   GET  /api/accounts/{accountNumber}                 -> BankAccountDto (includes current balance)
 *   POST /api/accounts/{accountNumber}/debit            -> DebitResponse
 */
@FeignClient(
        name = "${services.banking-service.name}",
        url = "${services.banking-service.url}",
        configuration = FeignAuthInterceptor.class
)
public interface BankingServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    BankAccountDto getAccount(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/api/accounts/{accountNumber}/debit")
    DebitResponse debitAccount(@PathVariable("accountNumber") String accountNumber,
                                @RequestBody DebitRequest request);
}
