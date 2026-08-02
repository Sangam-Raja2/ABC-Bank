package com.sangam.abcbank.emiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;


/**
 * Talks to credit-card-service and loan-service to:
 *  (a) confirm the source transaction/account is valid and belongs to the caller, and
 *  (b) actually reduce the outstanding credit card bill / loan amount once the EMI
 *      transition is confirmed, so the source service's ledger reflects that this amount
 *      is now being repaid as EMI instead of as a card bill / regular loan installment.
 *
 * Endpoint paths below are best-guess based on your README's naming conventions - adjust
 * them to match the real controllers in credit-card-service / loan-service.
 */
@Component
public class DownstreamBankingClient {

    private final WebClient webClient;
    private final String creditCardServiceBaseUrl;
    private final String loanServiceBaseUrl;

    public DownstreamBankingClient(WebClient.Builder builder,
                                   @Value("${services.credit-card-service.base-url}") String creditCardServiceBaseUrl,
                                   @Value("${services.loan-service.base-url}") String loanServiceBaseUrl) {
        this.webClient = builder.build();
        this.creditCardServiceBaseUrl = creditCardServiceBaseUrl;
        this.loanServiceBaseUrl = loanServiceBaseUrl;
    }

    public boolean verifyCreditCardTransaction(String transactionId, String bearerToken) {
        return safeVerify(creditCardServiceBaseUrl + "/api/credit-cards/transactions/" + transactionId, bearerToken);
    }

    public boolean verifyLoanAccount(String loanAccountId, String bearerToken) {
        return safeVerify(loanServiceBaseUrl + "/api/loans/" + loanAccountId, bearerToken);
    }

    /**
     * Reduces the outstanding credit card bill by the converted amount (or marks the
     * transaction as converted, depending on how credit-card-service models this).
     * Throws DownstreamAdjustmentException on any failure so the caller can fail the
     * whole transition rather than silently leaving both ledgers inconsistent.
     */
    public void adjustCreditCardBill(String transactionId, BigDecimal convertedAmount, String bearerToken) {
        String url = creditCardServiceBaseUrl + "/api/credit-cards/transactions/" + transactionId + "/convert-to-emi";
        adjust(url, convertedAmount, bearerToken, "credit card bill");
    }

    /**
     * Reduces the outstanding loan amount by the converted amount, once that portion has
     * been carved out into its own EMI schedule here.
     */
    public void adjustLoanAmount(String loanAccountId, BigDecimal convertedAmount, String bearerToken) {
        String url = loanServiceBaseUrl + "/api/loans/" + loanAccountId + "/convert-to-emi";
        adjust(url, convertedAmount, bearerToken, "loan amount");
    }

    private void adjust(String url, BigDecimal convertedAmount, String bearerToken, String what) {
        try {
            webClient.patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + bearerToken)
                    .bodyValue(Map.of("convertedAmount", convertedAmount))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new DownstreamAdjustmentException(
                    "Failed to adjust " + what + " at " + url + ": HTTP " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new DownstreamAdjustmentException(
                    "Failed to adjust " + what + " at " + url + ": " + e.getMessage(), e);
        }
    }

    private boolean safeVerify(String url, String bearerToken) {
        try {
            webClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            return false;
        } catch (Exception e) {
            // downstream unreachable - fail closed
            return false;
        }
    }

    public static class DownstreamAdjustmentException extends RuntimeException {
        public DownstreamAdjustmentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
