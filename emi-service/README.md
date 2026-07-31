# emi-service (ABC-Bank microservices)

A Spring Boot microservice that:
- Calculates EMIs (standalone calculator + per-loan calculator)
- Fetches a loan by ID from **loan-service**, restricted to the loan owner (same username) or **ROLE_ADMIN**, enforced via `@PreAuthorize`
- Auto-pays the due EMI by checking **banking-service** for available balance, and only debits if funds are sufficient
- Runs a nightly scheduled job to auto-pay all due EMIs across active loans

## ⚠️ Important: this was built without direct access to your repo

I could not clone or browse `https://github.com/Sangam-Raja2/ABC-Bank.git` (GitHub blocks
automated fetches, and this sandbox has no live network for `git clone`). So this service is
built against a **documented, reasonable assumption** of what `user-service`, `banking-service`,
and `loan-service` expose — not their verified actual APIs.

Everywhere an assumption was made, it's marked `ASSUMPTION:` in the code comments. The three
places you'll most likely need to adjust are listed below. Send me the actual controller
code/DTOs (or the endpoint list) for those three services and I'll update the Feign clients,
DTO field names, and JWT claim names to match exactly.

### 1. `LoanServiceClient` (`client/LoanServiceClient.java`)
Assumed endpoints:
```
GET  /api/loans/{loanId}                    -> LoanDto (must include: loanId, username, accountNumber,
                                                principalAmount, annualInterestRate, tenureMonths,
                                                installmentsPaid, outstandingAmount, status)
PUT  /api/loans/{loanId}/installment-paid   -> records that one EMI was paid, updates outstanding balance
```
Also needed for the scheduler (not yet implemented, see below):
```
GET  /api/loans/due-today (or similar)      -> list of ACTIVE loans whose next installment is due
```

### 2. `BankingServiceClient` (`client/BankingServiceClient.java`)
Assumed endpoints:
```
GET  /api/accounts/{accountNumber}          -> BankAccountDto (accountNumber, username, balance, status)
POST /api/accounts/{accountNumber}/debit    -> DebitResponse (transactionId, status, balanceAfter)
```

### 3. JWT claims from `user-service` (`config/SecurityConfig.java`)
Assumed the JWT has:
- `preferred_username` claim = username (used as `Authentication.getName()`)
- `roles` or `authorities` claim = list like `["ADMIN"]` or `["ROLE_ADMIN"]`

If `user-service` issues tokens differently (e.g. username is in `sub`, or roles come
from a `scope` string), update `jwtAuthenticationConverter()` in `SecurityConfig`.

Also set `spring.security.oauth2.resourceserver.jwt.issuer-uri` (or `jwk-set-uri`) in
`application.yml` to match user-service's actual token issuer, so tokens it mints are
accepted here too — all services must trust the same signing key/issuer.

## How access control works

```java
@GetMapping("/loans/{loanId}")
@PreAuthorize("@emiSecurityService.canAccessLoan(#loanId, authentication)")
public ResponseEntity<LoanDto> getLoan(@PathVariable Long loanId) { ... }
```

`EmiSecurityService.canAccessLoan()`:
1. If caller has `ROLE_ADMIN` → allow.
2. Otherwise, fetch the loan from loan-service and compare `loan.getUsername()` to
   `authentication.getName()` → allow only on exact match.

This means **loan-service is the source of truth for ownership** — emi-service never
trusts a username passed in the request body/path, only what loan-service returns.

The incoming caller's `Authorization: Bearer <jwt>` header is propagated to loan-service
and banking-service via `FeignAuthInterceptor`, so those services can also independently
enforce their own security on the same request.

## How EMI auto-payment works

`EmiService.attemptAutoPayment()`:
1. Fetch the account from banking-service.
2. **Compare available balance to the EMI amount — if insufficient, stop. No debit is attempted.**
   The attempt is logged as `FAILED_INSUFFICIENT_FUNDS` in the local `emi_payments` ledger.
3. If sufficient, call banking-service's debit endpoint with an idempotency key
   (`EMI-{loanId}-{installmentNumber}`).
4. On successful debit, call loan-service to record the installment as paid and update
   the outstanding balance.
5. If the debit succeeds but the loan-service update fails, this is logged as **CRITICAL**
   for manual reconciliation (money moved, but the loan ledger wasn't updated) — the local
   payment record still reflects `PAID` with the banking transaction ID attached, so nothing
   is silently lost.

This same method is used both by the on-demand endpoint (`POST /api/emi/loans/{loanId}/pay-next`)
and the nightly scheduled job (`AutoPaymentScheduler`), so behavior is identical either way.

### ⚠️ TODO before running the scheduler
`AutoPaymentScheduler.fetchDueLoans()` currently throws `UnsupportedOperationException` — it's a
placeholder. It needs a real "give me all ACTIVE loans due today" endpoint from loan-service, or
a paginated scan you compute due-ness from locally (`startDate` + `installmentsPaid`). I left this
unresolved because I don't know if that endpoint already exists in your loan-service.

Also note: the scheduler runs without an inbound HTTP request, so `FeignAuthInterceptor` has
nothing to propagate. You'll need a service-to-service credential (client-credentials JWT issued
to `emi-service`, an internal API key, or mTLS) that loan-service/banking-service trust for
non-user-initiated calls — decide this based on how the rest of ABC-Bank does service-to-service auth.

## EMI calculator formula

Standard reducing-balance formula:

```
EMI = [P × R × (1+R)^N] / [(1+R)^N − 1]
```
- P = principal
- R = monthly interest rate = annual rate / 12 / 100
- N = tenure in months

`EmiCalculatorService` also produces a full month-by-month amortization schedule
(principal component, interest component, remaining balance) — see
`POST /api/emi/calculate`.

## Endpoints

| Method | Path                              | Auth                                   | Description                          |
|--------|-----------------------------------|-----------------------------------------|---------------------------------------|
| POST   | `/api/emi/calculate`              | any authenticated user                  | Standalone EMI calculator             |
| GET    | `/api/emi/loans/{loanId}`         | owner (same username) or ROLE_ADMIN     | Fetch loan detail via loan-service     |
| GET    | `/api/emi/loans/{loanId}/calculate` | owner (same username) or ROLE_ADMIN   | EMI for that loan's actual terms       |
| POST   | `/api/emi/loans/{loanId}/pay-next` | owner (same username) or ROLE_ADMIN    | Trigger next EMI payment now           |

## Running locally

```bash
mvn clean install
mvn spring-boot:run
```

Defaults to an in-memory H2 database (`application.yml`) so it boots without external
dependencies for local testing of the calculator endpoint. Swap the datasource block to
MySQL/Postgres for anything persistent, and point `services.loan-service.url` /
`services.banking-service.url` (or Eureka) at your real service instances.

> Note: I wasn't able to run `mvn` in this sandbox either (no outbound network to
> download dependencies), so this hasn't been compiled here — please build it locally
> and share any compile errors if something doesn't line up with your Spring Boot/Cloud
> version, and I'll fix it immediately.
