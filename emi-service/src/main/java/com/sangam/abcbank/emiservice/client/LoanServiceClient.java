package com.sangam.abcbank.emiservice.client;

import com.sangam.abcbank.emiservice.config.FeignAuthInterceptor;
import com.sangam.abcbank.emiservice.dto.EmiInstallmentUpdateRequest;
import com.sangam.abcbank.emiservice.dto.LoanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ASSUMPTIONS about loan-service's exposed API (adjust paths once confirmed from the repo):
 *   GET  /api/loans/{loanId}                          -> LoanDto
 *   PUT  /api/loans/{loanId}/installment-paid          -> LoanDto (updated)
 *   GET  /api/loans/user/{username}                    -> list of loans for a user (not used yet, handy later)
 *
 * name = "loan-service" resolves via Eureka if service discovery is on the classpath and registered;
 * url = the fallback direct base URL from application.yml (services.loan-service.url) is used otherwise.
 */
@FeignClient(
        name = "${services.loan-service.name}",
        url = "${services.loan-service.url}",
        configuration = FeignAuthInterceptor.class
)
public interface LoanServiceClient {

    @GetMapping("/api/loans/{loanId}")
    LoanDto getLoanById(@PathVariable("loanId") Long loanId);

    @PutMapping("/api/loans/{loanId}/installment-paid")
    LoanDto recordInstallmentPaid(@PathVariable("loanId") Long loanId,
                                   @RequestBody EmiInstallmentUpdateRequest request);
}
